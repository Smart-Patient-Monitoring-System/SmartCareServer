package com.example.mainservice.service.impl;

import com.example.mainservice.dto.MedicalEventCreateDTO;
import com.example.mainservice.dto.MedicalEventResponseDTO;
import com.example.mainservice.dto.MedicalSummaryDTO;
import com.example.mainservice.entity.MedicalEvent;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.MedicalEventRepository;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.service.MedicalEventService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class MedicalEventServiceImpl implements MedicalEventService {

    private final MedicalEventRepository repo;
    private final PatientRepo patientRepo;
    private final ObjectMapper objectMapper;

    public MedicalEventServiceImpl(MedicalEventRepository repo, PatientRepo patientRepo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.patientRepo = patientRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addEvent(Long patientId, MedicalEventCreateDTO dto, String createdBy) {
        if (patientId == null) throw new IllegalArgumentException("patientId required");
        if (dto == null) throw new IllegalArgumentException("Body required");
        if (dto.getType() == null || dto.getType().trim().isEmpty()) throw new IllegalArgumentException("type required");
        if (dto.getRecordedAt() == null || dto.getRecordedAt().trim().isEmpty()) throw new IllegalArgumentException("recordedAt required");
        if (dto.getPayload() == null) dto.setPayload(new HashMap<>());

        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        LocalDateTime recordedAt = LocalDateTime.parse(dto.getRecordedAt());

        try {
            String payloadJson = objectMapper.writeValueAsString(dto.getPayload());

            MedicalEvent event = MedicalEvent.builder()
                    .patient(patient)
                    .type(dto.getType().trim().toUpperCase())
                    .recordedAt(recordedAt)
                    .payloadJson(payloadJson)
                    .createdBy(createdBy)
                    .build();

            repo.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save medical event: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MedicalEventResponseDTO> getEvents(Long patientId, String from, String to) {
        if (patientId == null) throw new IllegalArgumentException("patientId required");

        LocalDateTime fromDt = null;
        LocalDateTime toDt = null;

        if (from != null && !from.isBlank()) {
            LocalDate d = LocalDate.parse(from);
            fromDt = d.atStartOfDay();
        }
        if (to != null && !to.isBlank()) {
            LocalDate d = LocalDate.parse(to);
            toDt = d.atTime(LocalTime.MAX);
        }

        List<MedicalEvent> events = (fromDt == null && toDt == null)
                ? repo.findByPatient_IdOrderByRecordedAtDesc(patientId)
                : repo.findInRange(patientId, fromDt, toDt);

        return events.stream().map(this::toDto).toList();
    }

    @Override
    public MedicalSummaryDTO getSummary(Long patientId) {
        // latest vitals
        Map<String, Object> latestVitals = null;
        List<Map<String, Object>> meds = new ArrayList<>();
        List<Map<String, Object>> diagnoses = new ArrayList<>();
        List<Map<String, Object>> allergies = new ArrayList<>();

        // VITALS
        List<MedicalEvent> vitals = repo.findByPatientAndType(patientId, "VITALS");
        if (!vitals.isEmpty()) {
            MedicalEvent latest = vitals.get(0);
            Map<String, Object> payload = readPayload(latest.getPayloadJson());
            // include recordedAt so frontend can show time
            payload.put("recordedAt", latest.getRecordedAt().toString());
            latestVitals = payload;
        }

        // MEDICATION (take latest 10)
        List<MedicalEvent> medEvents = repo.findByPatientAndType(patientId, "MEDICATION");
        for (int i = 0; i < Math.min(10, medEvents.size()); i++) {
            MedicalEvent e = medEvents.get(i);
            Map<String, Object> payload = readPayload(e.getPayloadJson());
            payload.put("recordedAt", e.getRecordedAt().toString());
            meds.add(payload);
        }

        // DIAGNOSIS (take latest 10)
        List<MedicalEvent> diagEvents = repo.findByPatientAndType(patientId, "DIAGNOSIS");
        for (int i = 0; i < Math.min(10, diagEvents.size()); i++) {
            MedicalEvent e = diagEvents.get(i);
            Map<String, Object> payload = readPayload(e.getPayloadJson());
            payload.put("recordedAt", e.getRecordedAt().toString());
            diagnoses.add(payload);
        }

        // ALLERGY (take latest 10)
        List<MedicalEvent> allergyEvents = repo.findByPatientAndType(patientId, "ALLERGY");
        for (int i = 0; i < Math.min(10, allergyEvents.size()); i++) {
            MedicalEvent e = allergyEvents.get(i);
            Map<String, Object> payload = readPayload(e.getPayloadJson());
            payload.put("recordedAt", e.getRecordedAt().toString());
            allergies.add(payload);
        }

        return new MedicalSummaryDTO(latestVitals, meds, diagnoses, allergies);
    }

    private MedicalEventResponseDTO toDto(MedicalEvent e) {
        Map<String, Object> payload = readPayload(e.getPayloadJson());
        return new MedicalEventResponseDTO(
                e.getId(),
                e.getType(),
                e.getRecordedAt(),
                payload,
                e.getCreatedBy()
        );
    }

    private Map<String, Object> readPayload(String json) {
        try {
            if (json == null || json.isBlank()) return new HashMap<>();
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }
}
