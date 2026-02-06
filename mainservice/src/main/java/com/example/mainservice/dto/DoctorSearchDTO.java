package com.example.mainservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorSearchDTO {
    private Long id;              // Doctor's database ID
    private String name;          // Doctor's name
    private String doctorRegNo;   // Registration number (like D001)
    private String hospital;      // Hospital name
    private String position;      // Job title (Cardiologist, etc.)
    private String email;         // Email for contact
    private String contactNo;     // Phone number

    // You can add this later if you implement profile pictures
    // private String profilePicture;
}