package com.example.mainservice.controller;

import com.example.mainservice.dto.AdminDTO;
import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.entity.Admin;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.repository.AdminRepo;
import com.example.mainservice.service.DoctorService;
import com.example.mainservice.service.EmailService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AdminRepo adminRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/doctor/create")
    public ResponseEntity<?> createDoctorByAdmin(@Valid @RequestBody DoctorDTO doctorDto) {
        try {
            // Create the doctor
            Doctor doctor = doctorService.create(doctorDto);

            // Send credentials email to doctor
            try {
                emailService.sendDoctorCredentialsEmail(
                    doctor.getEmail(),
                    doctor.getName(),
                    doctor.getUsername(),
                    doctorDto.getPassword() // Use the plain password before encryption
                );
            } catch (Exception e) {
                // Log error but don't fail the request if email fails
                // The doctor is already created
                System.err.println("Failed to send credentials email: " + e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Doctor created successfully and credentials sent via email");
            response.put("doctor", doctor);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during doctor creation: " + e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody AdminDTO adminDto) {
        try {
            // Check if username or email already exists
            if (adminRepo.existsByUsername(adminDto.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Username already exists"));
            }
            if (adminRepo.existsByEmail(adminDto.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Email already exists"));
            }

            if (adminDto.getPassword() == null || adminDto.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Password is required"));
            }

            Admin admin = Admin.builder()
                    .name(adminDto.getName())
                    .email(adminDto.getEmail())
                    .username(adminDto.getUsername())
                    .password(passwordEncoder.encode(adminDto.getPassword()))
                    .build();

            admin = adminRepo.save(admin);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin created successfully");
            response.put("admin", convertToDTO(admin));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during admin creation: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllAdmins() {
        try {
            List<AdminDTO> admins = adminRepo.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while fetching admins: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        try {
            Admin admin = adminRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            return ResponseEntity.ok(convertToDTO(admin));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id, @Valid @RequestBody AdminDTO adminDto) {
        try {
            Admin admin = adminRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            // Check if username is being changed and if it's already taken
            if (!admin.getUsername().equals(adminDto.getUsername()) &&
                    adminRepo.existsByUsername(adminDto.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Username already exists"));
            }

            // Check if email is being changed and if it's already taken
            if (!admin.getEmail().equals(adminDto.getEmail()) &&
                    adminRepo.existsByEmail(adminDto.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Email already exists"));
            }

            admin.setName(adminDto.getName());
            admin.setEmail(adminDto.getEmail());
            admin.setUsername(adminDto.getUsername());

            // Update password only if provided
            if (adminDto.getPassword() != null && !adminDto.getPassword().trim().isEmpty()) {
                admin.setPassword(passwordEncoder.encode(adminDto.getPassword()));
            }

            admin = adminRepo.save(admin);

            return ResponseEntity.ok(convertToDTO(admin));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        try {
            if (!adminRepo.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Admin not found"));
            }

            // Prevent deleting the last admin
            long adminCount = adminRepo.count();
            if (adminCount <= 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Cannot delete the last admin account"));
            }

            adminRepo.deleteById(id);
            return ResponseEntity.ok(new SuccessResponse("Admin deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred: " + e.getMessage()));
        }
    }

    private AdminDTO convertToDTO(Admin admin) {
        return AdminDTO.builder()
                .id(admin.getId())
                .name(admin.getName())
                .email(admin.getEmail())
                .username(admin.getUsername())
                .build();
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
