package com.example.mainservice.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DoctorDTO {
    private  Long Id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Date of Birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "NIC No is required")
    private String nicNo;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Contact No is required")
    private String contactNo;

    @NotBlank(message = "Doctor Registration No is required")
    private String doctorRegNo;

    @NotBlank(message = "Position is required")
    private String position;

    @NotBlank(message = "Hospital is required")
    private String hospital;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
