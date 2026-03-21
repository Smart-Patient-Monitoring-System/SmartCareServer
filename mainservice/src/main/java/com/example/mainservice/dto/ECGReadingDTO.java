package com.example.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ECGReadingDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private String prediction;
    private double probability;
    private int meanHR;
    private double sdnn;
    private double rmssd;
    private int beats;
    private String status;
    private String rationale;
    private String waveformJson;
    private LocalDateTime recordedAt;
}
