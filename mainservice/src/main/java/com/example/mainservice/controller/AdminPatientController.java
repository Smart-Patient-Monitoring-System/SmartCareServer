package com.example.mainservice.controller;

import com.example.mainservice.dto.PatientDTO;
import com.example.mainservice.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/patient")
@RequiredArgsConstructor
public class AdminPatientController {

    private final PatientService patientService;

    @PostMapping("/create")
    public ResponseEntity<?> createPatient(@RequestBody PatientDTO patientDto) {
        try {
            return ResponseEntity.ok(patientService.create(patientDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/patient/update/{id}
     *
     * The regex :.+ on the path variable is required because Spring Boot 3
     * uses PathPatternParser by default, which treats trailing dots as literal
     * characters rather than stripping them as format extensions.
     * Without the regex, a request to /update/1 from some HTTP clients that
     * append a trailing dot (/update/1.) causes Spring to fail matching the
     * route entirely, returning "No static resource" 404.
     * The regex [0-9]+ ensures only numeric IDs are accepted.
     */
    @PutMapping("/update/{id:[0-9]+}")
    public ResponseEntity<?> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientDTO patientDto
    ) {
        try {
            return ResponseEntity.ok(patientService.updatePatient(id, patientDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update patient: " + e.getMessage()));
        }
    }

    /**
     * PATCH /api/admin/patient/update/{id}
     * Same endpoint accepting PATCH in case frontend uses PATCH instead of PUT.
     */
    @PatchMapping("/update/{id:[0-9]+}")
    public ResponseEntity<?> patchPatient(
            @PathVariable Long id,
            @RequestBody PatientDTO patientDto
    ) {
        return updatePatient(id, patientDto);
    }

    /**
     * DELETE /api/admin/patient/delete/{id}
     */
    @DeleteMapping("/delete/{id:[0-9]+}")
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.ok(Map.of("message", "Patient deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}