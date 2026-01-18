// File: src/main/java/com/example/mainservice/service/impl/VitalSignsServiceImpl.java

package com.example.mainservice.service.impl;

import com.example.mainservice.dto.VitalSignsDTO;
import com.example.mainservice.entity.VitalSigns;
import com.example.mainservice.repository.VitalSignsRepository;
import com.example.mainservice.service.VitalSignsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class VitalSignsServiceImpl implements VitalSignsService {

    private static final Logger logger = LoggerFactory.getLogger(VitalSignsServiceImpl.class);
    private final VitalSignsRepository repository;

    public VitalSignsServiceImpl(VitalSignsRepository repository) {
        this.repository = repository;
    }

    @Override
    public VitalSigns saveVitalSigns(VitalSignsDTO dto, Long patientId) {
        logger.info("Saving vital signs for patient ID: {}", patientId);

        try {
            // Validate that at least one vital sign is provided
            if (!hasAnyVitalSign(dto)) {
                throw new IllegalArgumentException("At least one vital sign measurement must be provided");
            }

            // Validate blood pressure pairs
            if ((dto.getBloodPressureSystolic() != null && dto.getBloodPressureDiastolic() == null) ||
                    (dto.getBloodPressureSystolic() == null && dto.getBloodPressureDiastolic() != null)) {
                throw new IllegalArgumentException("Both systolic and diastolic blood pressure values must be provided together");
            }

            // Parse date and time
            LocalDate date = LocalDate.parse(dto.getDate());
            LocalTime time = LocalTime.parse(dto.getTime());
            LocalDateTime measurementDateTime = LocalDateTime.of(date, time);

            // Create entity
            VitalSigns vitalSigns = new VitalSigns();
            vitalSigns.setBloodPressureSystolic(dto.getBloodPressureSystolic());
            vitalSigns.setBloodPressureDiastolic(dto.getBloodPressureDiastolic());
            vitalSigns.setBloodSugar(dto.getBloodSugar());
            vitalSigns.setTemperature(dto.getTemperature());
            vitalSigns.setHeartRate(dto.getHeartRate());
            vitalSigns.setSpo2(dto.getSpo2());
            vitalSigns.setWeight(dto.getWeight());
            vitalSigns.setMeasurementDateTime(measurementDateTime);
            vitalSigns.setNotes(dto.getNotes());
            vitalSigns.setPatientId(patientId);

            VitalSigns saved = repository.save(vitalSigns);
            logger.info("Vital signs saved successfully with ID: {}", saved.getId());
            return saved;

        } catch (Exception e) {
            logger.error("Failed to save vital signs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save vital signs: " + e.getMessage(), e);
        }
    }

    @Override
    public List<VitalSigns> getPatientVitalSigns(Long patientId) {
        logger.info("Fetching vital signs for patient ID: {}", patientId);
        return repository.findByPatientIdOrderByMeasurementDateTimeDesc(patientId);
    }

    @Override
    public VitalSigns getLatestVitalSigns(Long patientId) {
        logger.info("Fetching latest vital signs for patient ID: {}", patientId);
        return repository.findFirstByPatientIdOrderByMeasurementDateTimeDesc(patientId);
    }

    @Override
    public VitalSigns getVitalSignsById(Long id) {
        logger.info("Fetching vital signs by ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vital signs not found with ID: " + id));
    }

    @Override
    public void deleteVitalSigns(Long id) {
        logger.info("Deleting vital signs ID: {}", id);
        if (!repository.existsById(id)) {
            throw new RuntimeException("Vital signs not found with ID: " + id);
        }
        repository.deleteById(id);
        logger.info("Vital signs deleted successfully");
    }

    private boolean hasAnyVitalSign(VitalSignsDTO dto) {
        return dto.getBloodPressureSystolic() != null ||
                dto.getBloodSugar() != null ||
                dto.getTemperature() != null ||
                dto.getHeartRate() != null ||
                dto.getSpo2() != null ||
                dto.getWeight() != null;
    }
}