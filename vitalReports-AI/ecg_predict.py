import sys
import os
import json
import numpy as np
import tensorflow as tf

WINDOW_SIZE = 180
FS = 360

# ─── Load trained model ───
script_dir = os.path.dirname(os.path.abspath(__file__))
model_path = os.path.join(script_dir, "ecg_cnn_mitbih.keras")
model = tf.keras.models.load_model(model_path)


def detect_r_peaks(ecg, fs):
    """Detect R-peaks using simple peak detection (no annotation file needed)."""
    from scipy.signal import find_peaks

    # Normalize signal for peak detection
    ecg_norm = (ecg - np.mean(ecg)) / np.std(ecg)

    # Minimum distance between peaks: at least 200ms apart (300 bpm max)
    min_distance = int(0.2 * fs)

    # Find peaks with minimum height and distance
    peaks, properties = find_peaks(
        ecg_norm,
        height=0.5,            # peaks must be above 0.5 std devs
        distance=min_distance,  # at least 200ms apart
        prominence=0.5          # peaks must be prominent
    )

    # If too few peaks found, try with lower thresholds
    if len(peaks) < 5:
        peaks, properties = find_peaks(
            ecg_norm,
            height=0.3,
            distance=min_distance,
            prominence=0.3
        )

    return peaks


def parse_ecg_files(record_path):
    """Parse .dat + .hea files using wfdb and extract beats using R-peak detection."""
    import wfdb

    record = wfdb.rdrecord(record_path)
    ecg = record.p_signal[:, 0]  # MLII lead (first channel)
    fs = record.fs

    # Try to load annotation file, fall back to algorithmic R-peak detection
    try:
        annotation = wfdb.rdann(record_path, "atr")
        r_peaks = annotation.sample
    except Exception:
        # No annotation file — detect R-peaks algorithmically
        r_peaks = detect_r_peaks(ecg, fs)

    beats = []

    for r_peak in r_peaks:
        start = r_peak - WINDOW_SIZE // 2
        end = r_peak + WINDOW_SIZE // 2

        if start < 0 or end > len(ecg):
            continue

        beat = ecg[start:end]
        beats.append(beat)

    return ecg, fs, beats, r_peaks


def analyze_from_files(record_path):
    """Full analysis: parse files, run CNN on each beat, return results."""
    ecg, fs, beats, r_peaks = parse_ecg_files(record_path)

    if len(beats) == 0:
        return {
            "prediction": "No beats detected",
            "probability": 0.0,
            "waveform": ecg[:3600].tolist(),
            "fs": int(fs),
            "meanHR": 0,
            "sdnn": 0,
            "rmssd": 0,
            "beats": 0,
            "status": "Unable to analyze",
            "rationale": "No valid beats were detected in the uploaded ECG."
        }

    # Normalize and reshape beats for CNN
    X = np.array(beats)
    max_val = np.max(np.abs(X))
    if max_val > 0:
        X = X / max_val
    X = X.reshape(-1, WINDOW_SIZE, 1)

    # Predict all beats
    predictions = model.predict(X, verbose=0)
    abnormal_count = int(np.sum(predictions > 0.5))
    normal_count = len(predictions) - abnormal_count
    avg_prob = float(np.mean(predictions))

    # HRV metrics from R-peaks
    rr_intervals = np.diff(r_peaks) / fs * 1000  # in ms
    mean_hr = int(60000 / np.mean(rr_intervals)) if len(rr_intervals) > 0 else 0
    sdnn = round(float(np.std(rr_intervals)), 2) if len(rr_intervals) > 1 else 0
    rmssd = round(float(np.sqrt(np.mean(np.diff(rr_intervals) ** 2))), 2) if len(rr_intervals) > 1 else 0

    # Overall status
    if abnormal_count / len(predictions) > 0.3:
        status = "Abnormal"
        rationale = (f"CNN detected {abnormal_count} abnormal beats out of {len(predictions)} total. "
                     f"Mean probability: {avg_prob:.2f}. Possible arrhythmia — further review recommended.")
    else:
        status = "Normal"
        rationale = (f"CNN classified {normal_count}/{len(predictions)} beats as normal. "
                     f"Mean probability: {avg_prob:.2f}. ECG appears within normal limits.")

    # Return only first 10 seconds of waveform for graph display
    waveform_samples = min(len(ecg), int(fs * 10))

    return {
        "prediction": status,
        "probability": round(avg_prob, 4),
        "waveform": ecg[:waveform_samples].tolist(),
        "fs": int(fs),
        "meanHR": mean_hr,
        "sdnn": sdnn,
        "rmssd": rmssd,
        "beats": len(predictions),
        "status": status,
        "rationale": rationale
    }


def analyze_from_signal(signal_json):
    """Original mode: analyze a single 180-sample signal array."""
    input_data = json.loads(signal_json)
    signal = np.array(input_data["signal"])
    signal = signal / np.max(np.abs(signal))
    signal = signal.reshape(1, WINDOW_SIZE, 1)

    prob = float(model.predict(signal, verbose=0)[0][0])
    label = "Abnormal" if prob > 0.5 else "Normal"

    return {
        "prediction": label,
        "probability": round(prob, 4)
    }


if __name__ == "__main__":
    mode = sys.argv[1]

    if mode == "file":
        record_path = sys.argv[2]
        result = analyze_from_files(record_path)
    else:
        json_str = sys.argv[2]
        result = analyze_from_signal(json_str)

    print(json.dumps(result))
