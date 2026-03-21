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
public class CriticalAlertDTO {
    private Long patientId;
    private String patientName;
    private String room;
    private String alertTitle;
    private String description;
    private String severity;
    private String currentValue;
    private String normalRange;
    private LocalDateTime recordedAt;
}
