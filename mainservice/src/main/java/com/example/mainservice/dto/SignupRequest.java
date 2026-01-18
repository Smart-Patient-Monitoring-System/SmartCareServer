package com.example.mainservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Date of Birth is required")
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotBlank(message = "NIC No is required")
    private String nicNo;
    
    @NotBlank(message = "Gender is required")
    private String gender;
    
    @NotBlank(message = "Contact No is required")
    private String contactNo;
    
    @NotBlank(message = "Guardian Name is required")
    private String guardianName;
    
    @NotBlank(message = "Guardian Contact No is required")
    private String guardianContactNo;
    
    @NotBlank(message = "Blood Type is required")
    private String bloodType;

    @NotBlank(message = "Role is required")
    private String role; // PATIENT, DOCTOR, NURSE, ADMIN
}
