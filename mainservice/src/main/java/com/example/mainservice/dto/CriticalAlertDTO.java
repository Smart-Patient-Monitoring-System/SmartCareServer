package com.example.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a critical alert for the Doctor Portal.
 * Generated on-the-fly from abnormal vital signs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriticalAlertDTO {

    private Long patientId;
    private String patientName;
    private String room;

    /** e.g. "High Heart Rate", "Low Oxygen Level" */
    private String alertTitle;

    /** e.g. "Heart rate is 132 bpm (normal: 60-100)" */
    private String description;

    /** CRITICAL, HIGH, MEDIUM */
    private String severity;

    /** The current abnormal value as display string */
    private String currentValue;

    /** The normal range as display string */
    private String normalRange;

    /** When the vital was recorded */
    private LocalDateTime recordedAt;
}
