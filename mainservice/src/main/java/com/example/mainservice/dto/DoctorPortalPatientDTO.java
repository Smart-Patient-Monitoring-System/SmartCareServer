package com.example.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPortalPatientDTO {
    private Long patientId;
    private String patientName;
    private String bloodType;
    private String contactNo;
    private String medicalConditions;
    private String city;
    private String district;
    private String address;
    private String gender;
    private LocalDate dateOfBirth;
    private String room;
    private Integer heartRate;
    private Double temperature;
    private Integer spo2;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private String riskLevel;
    private String status;
}
