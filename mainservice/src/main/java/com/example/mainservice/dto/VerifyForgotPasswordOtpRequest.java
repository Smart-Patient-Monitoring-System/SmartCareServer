package com.example.mainservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyForgotPasswordOtpRequest {
    @NotBlank(message = "Reset session id is required")
    private String resetSessionId;

    @NotBlank(message = "OTP is required")
    private String otp;
}

