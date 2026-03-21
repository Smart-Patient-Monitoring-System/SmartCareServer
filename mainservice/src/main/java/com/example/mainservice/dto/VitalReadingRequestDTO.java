package com.example.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalReadingRequestDTO {
    private int spo2;
    private int systolicBP;
    private int heartRate;
    private double temperature;
    private double bloodSugar;
    private Long timestamp;
}
