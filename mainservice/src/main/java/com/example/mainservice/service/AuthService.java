package com.example.mainservice.service;

import com.example.mainservice.dto.AuthResponse;
import com.example.mainservice.dto.LoginRequest;
import com.example.mainservice.dto.SignupRequest;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.security.CustomUserDetails;
import com.example.mainservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PatientRepo patientRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String token = jwtUtil.generateToken(userDetails, userDetails.getRole());

        return AuthResponse.builder()
                .token(token)
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getRole())
                .name(userDetails.getDisplayName())
                .build();
    }

    public AuthResponse signup(SignupRequest signupRequest) {
        // Currently only patient signup is supported via /api/auth/signup.
        // Doctors should be created via /api/doctor/create.
        String role = signupRequest.getRole().toUpperCase();
        if (!"PATIENT".equals(role)) {
            throw new RuntimeException("Only PATIENT signup is supported via this endpoint");
        }

        if (patientRepo.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (patientRepo.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        Patient patient = Patient.builder()
                .name(signupRequest.getName())
                .dateOfBirth(signupRequest.getDateOfBirth())
                .address(signupRequest.getAddress())
                .email(signupRequest.getEmail())
                .nicNo(signupRequest.getNicNo())
                .gender(signupRequest.getGender())
                .contactNo(signupRequest.getContactNo())
                .guardiansName(signupRequest.getGuardianName())
                .guardiansContactNo(signupRequest.getGuardianContactNo())
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .bloodType(signupRequest.getBloodType())
                .build();

        patient = patientRepo.save(patient);

        CustomUserDetails userDetails = new CustomUserDetails(
                patient.getId(),
                patient.getUsername(),
                patient.getPassword(),
                patient.getEmail(),
                patient.getName(),
                "PATIENT"
        );
        String token = jwtUtil.generateToken(userDetails, "PATIENT");

        return AuthResponse.builder()
                .token(token)
                .username(patient.getUsername())
                .email(patient.getEmail())
                .role("PATIENT")
                .name(patient.getName())
                .build();
    }
}
