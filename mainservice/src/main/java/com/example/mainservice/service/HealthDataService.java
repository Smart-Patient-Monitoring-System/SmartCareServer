package com.example.mainservice.service;

import com.example.mainservice.dto.WorkoutSessionDTO;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.entity.WorkoutSession;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.repository.WorkoutSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthDataService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final WorkoutSessionRepository workoutSessionRepository;
    private final PatientRepo patientRepo;

    /**
     * Upload a new workout session for a patient
     */
    @Transactional
    public WorkoutSessionDTO uploadWorkoutSession(Long patientId, String name, MultipartFile file) {
        // Find patient
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));

        try {
            // Read file content
            String jsonContent = new String(file.getBytes());

            // Parse JSON to determine source
            Map<String, Object> jsonData = OBJECT_MAPPER.readValue(jsonContent, Map.class);
            String source = determineSource(jsonData);

            // Calculate content hash
            String contentHash = calculateContentHash(jsonContent);

            // Check for duplicate
            if (workoutSessionRepository.existsByPatientIdAndContentHash(patientId, contentHash)) {
                throw new RuntimeException("Duplicate upload: This file has already been uploaded.");
            }

            // Create workout session
            WorkoutSession session = WorkoutSession.builder()
                    .patient(patient)
                    .name(name)
                    .sessionName(name)
                    .uploadDate(LocalDate.now())
                    .source(source)
                    .healthData(jsonContent)
                    .healthData(jsonContent)
                    .fileName(file.getOriginalFilename())
                    .contentHash(contentHash)
                    .build();

            WorkoutSession savedSession = workoutSessionRepository.save(session);

            log.info("Uploaded workout session '{}' for patient ID: {}", name, patientId);

            return convertToDTO(savedSession);
        } catch (IOException e) {
            log.error("Error processing health data file: {}", e.getMessage());
            throw new RuntimeException("Error processing health data file: " + e.getMessage());
        }
    }

    /**
     * Get all workout sessions for a patient
     */
    public List<WorkoutSessionDTO> getWorkoutSessions(Long patientId) {
        // Verify patient exists
        if (!patientRepo.existsById(patientId)) {
            throw new RuntimeException("Patient not found with ID: " + patientId);
        }

        List<WorkoutSession> sessions = workoutSessionRepository.findByPatientIdOrderByCreatedAtDesc(patientId);

        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update workout session name
     */
    @Transactional
    public WorkoutSessionDTO updateWorkoutSession(Long sessionId, String name) {
        WorkoutSession session = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found with ID: " + sessionId));

        session.setName(name);
        WorkoutSession updatedSession = workoutSessionRepository.save(session);

        log.info("Updated workout session ID: {} with new name: {}", sessionId, name);

        return convertToDTO(updatedSession);
    }

    /**
     * Delete workout session
     */
    @Transactional
    public void deleteWorkoutSession(Long sessionId, Long patientId) {
        // Verify session belongs to patient
        if (!workoutSessionRepository.existsByIdAndPatientId(sessionId, patientId)) {
            throw new RuntimeException("Workout session not found or does not belong to patient");
        }

        workoutSessionRepository.deleteById(sessionId);
        log.info("Deleted workout session ID: {} for patient ID: {}", sessionId, patientId);
    }

    /**
     * Determine the source of health data based on JSON structure
     */
    private String determineSource(Map<String, Object> jsonData) {
        if (jsonData.containsKey("data")) {
            Object data = jsonData.get("data");
            if (data instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) data;
                if (dataMap.containsKey("metrics")) {
                    return "Apple Health";
                } else if (dataMap.containsKey("workouts") || dataMap.containsKey("steps")) {
                    return "Custom Health Data";
                }
            }
        }
        if (jsonData.containsKey("bucket")) {
            return "Google Fit";
        }
        return "Custom Health Data";
    }

    private String calculateContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculating content hash", e);
        }
    }

    /**
     * Convert entity to DTO
     */
    private WorkoutSessionDTO convertToDTO(WorkoutSession session) {
        Object healthDataObj = null;
        try {
            healthDataObj = OBJECT_MAPPER.readValue(session.getHealthData(), Map.class);
        } catch (JsonProcessingException e) {
            log.warn("Error parsing health data JSON for session {}: {}", session.getId(), e.getMessage());
            // Return raw string if parsing fails
            healthDataObj = session.getHealthData();
        }

        return WorkoutSessionDTO.builder()
                .id(session.getId())
                .patientId(session.getPatient().getId())
                .name(session.getName())
                .uploadDate(session.getWorkoutDate())
                .source(session.getSource())
                .healthData(healthDataObj)
                .fileName(session.getFileName())
                .build();
    }
}
