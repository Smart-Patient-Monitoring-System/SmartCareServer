package com.example.mainservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyLoginOtpRequest {
    @NotBlank(message = "Login session id is required")
    private String loginSessionId;

    @NotBlank(message = "OTP is required")
    private String otp;
}

