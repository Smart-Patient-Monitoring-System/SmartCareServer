package com.example.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String username;
    private String email;
    private String role;
    private String name;
    private Long patientId;
    // OTP login support (used for PATIENT login step-1 response)
    @Builder.Default
    private boolean otpRequired = false;
    private String loginSessionId;
}
