package com.example.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ECGAnalysisResponseDTO {
    private String prediction;
    private double probability;
    private double meanHR;
    private double SDNN;
    private double RMSSD;
    private int beats;
    private String status;
    private String rationale;
    private int fs;
    private List<Double> waveform;
}
