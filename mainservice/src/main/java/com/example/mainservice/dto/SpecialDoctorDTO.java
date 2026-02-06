package com.example.mainservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialDoctorDTO {
    private Long id;                   // Auto-generated ID
    private String registrationNumber; // Hospital/medical registration number
    private String name;
    private String specialty;
    private Double consultationFee;
    private String profilePhoto;
    private String description;
    private String qualification;      // Matches entity
    private String phoneNumber;        // Matches entity
    private String email;
}