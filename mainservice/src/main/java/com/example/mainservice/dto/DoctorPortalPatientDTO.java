package com.example.mainservice.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * DTO for Doctor Portal - displays patient overview with vital signs.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DoctorPortalPatientDTO {
    private Long patientId;
    private String patientName;
    private String room; // From vital signs if available

    // Vital Signs
    private Integer heartRate;
    private Double temperature;
    private Integer spo2;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;

    // Risk Assessment
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String status; // STABLE, MONITORING, CRITICAL

    // Additional patient info
    private String bloodType;
    private String contactNo;
    private String medicalConditions;

    // Demographics
    private String city;
    private String district;
    private String address;
    private String gender;
    private LocalDate dateOfBirth;
}
