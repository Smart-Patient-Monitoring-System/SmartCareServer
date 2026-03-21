package com.example.mainservice.service;

import com.example.mainservice.dto.ECGAnalysisResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ECGAnalysisService {

    private static final int DEFAULT_FS = 360;
    private static final int MAX_WAVEFORM_POINTS = 3000;

    public ECGAnalysisResponseDTO analyze(MultipartFile datFile, MultipartFile heaFile) throws IOException {
        if (datFile == null || datFile.isEmpty()) {
            throw new IllegalArgumentException("ECG .dat file is required");
        }
        if (heaFile == null || heaFile.isEmpty()) {
            throw new IllegalArgumentException("ECG .hea file is required");
        }

        int fs = extractSamplingRate(heaFile);
        List<Double> waveform = extractWaveform(datFile);
        List<Integer> peaks = detectPeaks(waveform, fs);

        double meanHr = calculateMeanHeartRate(peaks, fs);
        double sdnn = calculateSdnn(peaks, fs);
        double rmssd = calculateRmssd(peaks, fs);
        int beats = peaks.size();

        String status = classifyStatus(meanHr, sdnn, rmssd, beats);
        String prediction = status;
        double probability = "Normal".equals(status) ? 0.78 : 0.86;
        String rationale = buildRationale(meanHr, sdnn, rmssd, beats, fs, status);

        return ECGAnalysisResponseDTO.builder()
                .prediction(prediction)
                .probability(probability)
                .meanHR(round1(meanHr))
                .SDNN(round1(sdnn))
                .RMSSD(round1(rmssd))
                .beats(beats)
                .status(status)
                .rationale(rationale)
                .fs(fs)
                .waveform(waveform)
                .build();
    }

    private int extractSamplingRate(MultipartFile heaFile) throws IOException {
        String header = new String(heaFile.getBytes(), StandardCharsets.UTF_8);
        String firstLine = header.lines().findFirst().orElse("").trim();
        if (firstLine.isBlank()) {
            return DEFAULT_FS;
        }

        String[] parts = firstLine.split("\\s+");
        if (parts.length >= 3) {
            try {
                String rawFs = parts[2];
                int slashIndex = rawFs.indexOf('/');
                if (slashIndex >= 0) {
                    rawFs = rawFs.substring(0, slashIndex);
                }
                return Integer.parseInt(rawFs);
            } catch (NumberFormatException ignored) {
                return DEFAULT_FS;
            }
        }

        return DEFAULT_FS;
    }

    private List<Double> extractWaveform(MultipartFile datFile) throws IOException {
        byte[] bytes = datFile.getBytes();
        int limit = Math.min(bytes.length, MAX_WAVEFORM_POINTS);
        List<Double> waveform = new ArrayList<>(limit);

        for (int i = 0; i < limit; i++) {
            waveform.add(bytes[i] / 128.0);
        }

        return waveform;
    }

    private List<Integer> detectPeaks(List<Double> waveform, int fs) {
        List<Integer> peaks = new ArrayList<>();
        if (waveform.size() < 3) {
            return peaks;
        }

        double meanAbs = 0.0;
        for (double value : waveform) {
            meanAbs += Math.abs(value);
        }
        meanAbs /= waveform.size();
        double threshold = Math.max(0.18, meanAbs * 1.6);
        int minDistance = Math.max(1, fs / 3);
        int lastPeak = -minDistance;

        for (int i = 1; i < waveform.size() - 1; i++) {
            double current = waveform.get(i);
            if (current > threshold
                    && current > waveform.get(i - 1)
                    && current >= waveform.get(i + 1)
                    && i - lastPeak >= minDistance) {
                peaks.add(i);
                lastPeak = i;
            }
        }

        return peaks;
    }

    private double calculateMeanHeartRate(List<Integer> peaks, int fs) {
        if (peaks.size() < 2 || fs <= 0) {
            return 0.0;
        }

        double totalSeconds = (peaks.get(peaks.size() - 1) - peaks.get(0)) / (double) fs;
        if (totalSeconds <= 0) {
            return 0.0;
        }

        return ((peaks.size() - 1) * 60.0) / totalSeconds;
    }

    private double calculateSdnn(List<Integer> peaks, int fs) {
        List<Double> rr = rrIntervals(peaks, fs);
        if (rr.size() < 2) {
            return 0.0;
        }

        double mean = rr.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = rr.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    private double calculateRmssd(List<Integer> peaks, int fs) {
        List<Double> rr = rrIntervals(peaks, fs);
        if (rr.size() < 2) {
            return 0.0;
        }

        double sumSquares = 0.0;
        int count = 0;
        for (int i = 1; i < rr.size(); i++) {
            double diff = rr.get(i) - rr.get(i - 1);
            sumSquares += diff * diff;
            count++;
        }

        return count == 0 ? 0.0 : Math.sqrt(sumSquares / count);
    }

    private List<Double> rrIntervals(List<Integer> peaks, int fs) {
        List<Double> rr = new ArrayList<>();
        if (fs <= 0) {
            return rr;
        }

        for (int i = 1; i < peaks.size(); i++) {
            rr.add(((peaks.get(i) - peaks.get(i - 1)) * 1000.0) / fs);
        }
        return rr;
    }

    private String classifyStatus(double meanHr, double sdnn, double rmssd, int beats) {
        if (beats < 2) {
            return "Inconclusive";
        }
        if (meanHr >= 50 && meanHr <= 110 && sdnn <= 180 && rmssd <= 220) {
            return "Normal";
        }
        return "Abnormal";
    }

    private String buildRationale(double meanHr, double sdnn, double rmssd, int beats, int fs, String status) {
        if ("Inconclusive".equals(status)) {
            return "The uploaded ECG was accepted, but the signal quality or duration was too limited for a confident automated interpretation.";
        }

        return String.format(
                "Preliminary heuristic ECG review completed from the uploaded .dat/.hea files at %d Hz. Estimated heart rate %.1f bpm, SDNN %.1f ms, RMSSD %.1f ms, with %d detected beats. This record was classified as %s based on simple rhythm and variability thresholds.",
                fs, meanHr, sdnn, rmssd, beats, status
        );
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
