package com.example.vitalReports.domain.model;

import java.util.List;

public class ECGPredictionResponse {

    private String prediction;
    private double probability;
    private List<Double> waveform;
    private int fs;
    private int meanHR;
    private double SDNN;
    private double RMSSD;
    private int beats;
    private String status;
    private String rationale;

    public ECGPredictionResponse() {
    }

    // Getters and Setters

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public List<Double> getWaveform() {
        return waveform;
    }

    public void setWaveform(List<Double> waveform) {
        this.waveform = waveform;
    }

    public int getFs() {
        return fs;
    }

    public void setFs(int fs) {
        this.fs = fs;
    }

    public int getMeanHR() {
        return meanHR;
    }

    public void setMeanHR(int meanHR) {
        this.meanHR = meanHR;
    }

    public double getSDNN() {
        return SDNN;
    }

    public void setSDNN(double SDNN) {
        this.SDNN = SDNN;
    }

    public double getRMSSD() {
        return RMSSD;
    }

    public void setRMSSD(double RMSSD) {
        this.RMSSD = RMSSD;
    }

    public int getBeats() {
        return beats;
    }

    public void setBeats(int beats) {
        this.beats = beats;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }
}
