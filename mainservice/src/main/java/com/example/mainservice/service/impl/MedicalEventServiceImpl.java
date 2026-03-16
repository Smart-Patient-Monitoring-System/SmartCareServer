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
import com.example.mainservice.service.VitalSignsService;
import com.example.mainservice.dto.VitalSignsDTO;
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
    private final VitalSignsService vitalSignsService;

    public MedicalEventServiceImpl(MedicalEventRepository repo, PatientRepo patientRepo, ObjectMapper objectMapper,
            VitalSignsService vitalSignsService) {
        this.repo = repo;
        this.patientRepo = patientRepo;
        this.objectMapper = objectMapper;
        this.vitalSignsService = vitalSignsService;
    }

    @Override
    public void addEvent(Long patientId, MedicalEventCreateDTO dto, String createdBy) {
        if (patientId == null)
            throw new IllegalArgumentException("patientId required");
        if (dto == null)
            throw new IllegalArgumentException("Body required");
        if (dto.getType() == null || dto.getType().trim().isEmpty())
            throw new IllegalArgumentException("type required");
        if (dto.getRecordedAt() == null || dto.getRecordedAt().trim().isEmpty())
            throw new IllegalArgumentException("recordedAt required");
        if (dto.getPayload() == null)
            dto.setPayload(new HashMap<>());

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

            // Forward to VitalSigns table if type is VITALS (for AI evaluation & alerts)
            if ("VITALS".equals(event.getType())) {
                try {
                    VitalSignsDTO vDto = new VitalSignsDTO();
                    Map<String, Object> p = dto.getPayload();

                    if (p.containsKey("heartRate") && p.get("heartRate") != null
                            && !p.get("heartRate").toString().isEmpty())
                        vDto.setHeartRate(Integer.parseInt(p.get("heartRate").toString()));
                    if (p.containsKey("spo2") && p.get("spo2") != null && !p.get("spo2").toString().isEmpty())
                        vDto.setSpo2(Integer.parseInt(p.get("spo2").toString()));
                    if (p.containsKey("temp") && p.get("temp") != null && !p.get("temp").toString().isEmpty())
                        vDto.setTemperature(Double.parseDouble(p.get("temp").toString()));
                    if (p.containsKey("sugarLevel") && p.get("sugarLevel") != null
                            && !p.get("sugarLevel").toString().isEmpty())
                        vDto.setBloodSugar(Double.parseDouble(p.get("sugarLevel").toString()));

                    if (p.containsKey("bp") && p.get("bp") != null) {
                        String bp = p.get("bp").toString();
                        String[] parts = bp.split("/");
                        if (parts.length == 2) {
                            vDto.setBloodPressureSystolic(Integer.parseInt(parts[0].trim()));
                            vDto.setBloodPressureDiastolic(Integer.parseInt(parts[1].trim()));
                        }
                    }

                    vDto.setDate(recordedAt.toLocalDate().toString());
                    vDto.setTime(recordedAt.toLocalTime().toString());

                    vitalSignsService.saveVitalSigns(vDto, patientId);
                } catch (Exception ex) {
                    System.err.println("Failed to sync VITALS event to VitalSigns table: " + ex.getMessage());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to save medical event: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MedicalEventResponseDTO> getEvents(Long patientId, String from, String to) {
        if (patientId == null)
            throw new IllegalArgumentException("patientId required");

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
                e.getCreatedBy());
    }

    private Map<String, Object> readPayload(String json) {
        try {
            if (json == null || json.isBlank())
                return new HashMap<>();
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }
}
