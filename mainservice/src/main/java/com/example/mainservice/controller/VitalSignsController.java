package com.example.mainservice.controller;

import com.example.mainservice.dto.VitalSignsDTO;
import com.example.mainservice.entity.VitalSigns;
import com.example.mainservice.service.VitalSignsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vital-signs")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class VitalSignsController {

    private static final Logger logger = LoggerFactory.getLogger(VitalSignsController.class);
    private final VitalSignsService service;

    public VitalSignsController(VitalSignsService service) {
        this.service = service;
    }

    /**
     * Submit new vital signs data
     * POST /api/vital-signs/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitVitalSigns(@RequestBody VitalSignsDTO dto) {
        logger.info("Received vital signs submission");

        try {
            // TODO: Get actual patient ID from authentication context/JWT token
            // For now using hardcoded value
            Long patientId = 1L;

            VitalSigns saved = service.saveVitalSigns(dto, patientId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vital signs submitted successfully");
            response.put("id", saved.getId());
            response.put("measurementDateTime", saved.getMeasurementDateTime());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Failed to submit vital signs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to submit vital signs: " + e.getMessage()));
        }
    }

    /**
     * Get all vital signs for current patient
     * GET /api/vital-signs
     */
    @GetMapping
    public ResponseEntity<?> getVitalSigns() {
        logger.info("Fetching vital signs");

        try {
            // TODO: Get actual patient ID from authentication context/JWT token
            Long patientId = 1L;

            List<VitalSigns> vitalSigns = service.getPatientVitalSigns(patientId);
            return ResponseEntity.ok(vitalSigns);

        } catch (Exception e) {
            logger.error("Failed to fetch vital signs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch vital signs"));
        }
    }

    /**
     * Get latest vital signs for current patient
     * GET /api/vital-signs/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestVitalSigns() {
        logger.info("Fetching latest vital signs");

        try {
            // TODO: Get actual patient ID from authentication context/JWT token
            Long patientId = 1L;

            VitalSigns latest = service.getLatestVitalSigns(patientId);

            if (latest == null) {
                return ResponseEntity.ok(Map.of("message", "No vital signs found"));
            }

            return ResponseEntity.ok(latest);

        } catch (Exception e) {
            logger.error("Failed to fetch latest vital signs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch latest vital signs"));
        }
    }

    /**
     * Get specific vital signs by ID
     * GET /api/vital-signs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVitalSignsById(@PathVariable Long id) {
        logger.info("Fetching vital signs by ID: {}", id);

        try {
            VitalSigns vitalSigns = service.getVitalSignsById(id);
            return ResponseEntity.ok(vitalSigns);

        } catch (RuntimeException e) {
            logger.error("Vital signs not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Failed to fetch vital signs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch vital signs"));
        }
    }

    /**
     * Delete vital signs by ID
     * DELETE /api/vital-signs/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVitalSigns(@PathVariable Long id) {
        logger.info("Deleting vital signs ID: {}", id);

        try {
            service.deleteVitalSigns(id);
            return ResponseEntity.ok(Map.of("message", "Vital signs deleted successfully"));

        } catch (RuntimeException e) {
            logger.error("Failed to delete vital signs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Failed to delete vital signs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete vital signs"));
        }
    }
}
