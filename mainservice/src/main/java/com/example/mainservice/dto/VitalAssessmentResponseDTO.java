package com.example.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalAssessmentResponseDTO {
    private VitalStatusDTO vitalStatus;
    private String triageLevel;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VitalStatusDTO {
        private String spo2Status;
        private String pressureStatus;
        private String heartRateStatus;
        private String temperatureStatus;
        private String bloodSugarStatus;
    }
}
