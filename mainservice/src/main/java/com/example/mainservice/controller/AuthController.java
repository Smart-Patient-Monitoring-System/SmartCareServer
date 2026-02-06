package com.example.mainservice.controller;

import com.example.mainservice.dto.AuthResponse;
import com.example.mainservice.dto.ForgotPasswordRequest;
import com.example.mainservice.dto.ForgotPasswordResponse;
import com.example.mainservice.dto.ForgotPasswordOtpStartResponse;
import com.example.mainservice.dto.LoginRequest;
import com.example.mainservice.dto.ResetForgotPasswordRequest;
import com.example.mainservice.dto.ResetPasswordRequest;
import com.example.mainservice.dto.SignupRequest;
import com.example.mainservice.dto.VerifyForgotPasswordOtpRequest;
import com.example.mainservice.dto.VerifyLoginOtpRequest;
import com.example.mainservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Handle authentication failures (wrong username/password)
            // This must be caught before Exception since it extends Exception
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid email or password"));
        } catch (RuntimeException e) {
            // Handle role mismatch or other runtime exceptions
            // Return 400 Bad Request for role/validation errors, 403 for access denied
            HttpStatus status = e.getMessage().contains("role") || e.getMessage().contains("Role") 
                ? HttpStatus.BAD_REQUEST 
                : HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid email or password"));
        }
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> verifyPatientLoginOtp(@Valid @RequestBody VerifyLoginOtpRequest request) {
        try {
            AuthResponse response = authService.verifyPatientLoginOtp(
                    request.getLoginSessionId(),
                    request.getOtp()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during OTP verification"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            AuthResponse response = authService.signup(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during signup: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            ForgotPasswordResponse response = authService.forgotPassword(request);
            // Response includes resetToken and resetLink for development
            // In production, you may want to hide these and only return the message
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred: " + e.getMessage()));
        }
    }

    // New OTP-based forgot-password flow (preferred)
    @PostMapping("/forgot-password/request-otp")
    public ResponseEntity<?> forgotPasswordRequestOtp(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            ForgotPasswordOtpStartResponse response = authService.startForgotPasswordOtp(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> forgotPasswordVerifyOtp(@Valid @RequestBody VerifyForgotPasswordOtpRequest request) {
        try {
            authService.verifyForgotPasswordOtp(request.getResetSessionId(), request.getOtp());
            return ResponseEntity.ok(new SuccessResponse("OTP verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during OTP verification"));
        }
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> forgotPasswordReset(@Valid @RequestBody ResetForgotPasswordRequest request) {
        try {
            String role = authService.resetForgotPassword(request.getResetSessionId(), request.getNewPassword());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password has been reset successfully. You can now login with your new password.");
            response.put("role", role);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            String role = authService.resetPassword(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password has been reset successfully. You can now login with your new password.");
            response.put("role", role);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend is running!");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String errorMessage = errors.values().stream()
                .findFirst()
                .orElse("Validation failed");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage));
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }

    @Data
    @AllArgsConstructor
    public static class SuccessResponse {
        private String message;
    }
}