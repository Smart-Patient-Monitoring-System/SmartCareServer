package com.example.mainservice.controller;

import com.example.mainservice.dto.WorkoutSessionDTO;
import com.example.mainservice.dto.WorkoutSessionUpdateRequest;
import com.example.mainservice.service.HealthDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-data")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Slf4j
public class HealthDataController {

    private final HealthDataService healthDataService;

    /**
     * Upload a new workout session
     */
    @PostMapping("/workout-sessions")
    public ResponseEntity<?> uploadWorkoutSession(
            @RequestParam("patientId") Long patientId,
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading workout session '{}' for patient ID: {}", name, patientId);
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "File is required"));
            }

            WorkoutSessionDTO session = healthDataService.uploadWorkoutSession(patientId, name, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Workout session uploaded successfully");
            response.put("session", session);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Error uploading workout session: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error uploading workout session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred while uploading workout session"));
        }
    }

    /**
     * Get all workout sessions for a patient
     */
    @GetMapping("/workout-sessions/{patientId}")
    public ResponseEntity<?> getWorkoutSessions(@PathVariable Long patientId) {
        try {
            log.info("Fetching workout sessions for patient ID: {}", patientId);
            List<WorkoutSessionDTO> sessions = healthDataService.getWorkoutSessions(patientId);
            return ResponseEntity.ok(sessions);
        } catch (RuntimeException e) {
            log.error("Error fetching workout sessions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching workout sessions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred while fetching workout sessions"));
        }
    }

    /**
     * Update workout session name
     */
    @PutMapping("/workout-sessions/{sessionId}")
    public ResponseEntity<?> updateWorkoutSession(
            @PathVariable Long sessionId,
            @RequestBody WorkoutSessionUpdateRequest request) {
        try {
            log.info("Updating workout session ID: {} with name: {}", sessionId, request.getName());
            WorkoutSessionDTO session = healthDataService.updateWorkoutSession(sessionId, request.getName());
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            log.error("Error updating workout session: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating workout session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred while updating workout session"));
        }
    }

    /**
     * Delete workout session
     */
    @DeleteMapping("/workout-sessions/{sessionId}")
    public ResponseEntity<?> deleteWorkoutSession(
            @PathVariable Long sessionId,
            @RequestParam("patientId") Long patientId) {
        try {
            log.info("Deleting workout session ID: {} for patient ID: {}", sessionId, patientId);
            healthDataService.deleteWorkoutSession(sessionId, patientId);
            return ResponseEntity.ok(Map.of("message", "Workout session deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting workout session: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error deleting workout session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred while deleting workout session"));
        }
    }
}
