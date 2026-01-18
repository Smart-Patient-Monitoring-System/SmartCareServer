// File: src/main/java/com/example/mainservice/service/VitalSignsService.java

package com.example.mainservice.service;

import com.example.mainservice.dto.VitalSignsDTO;
import com.example.mainservice.entity.VitalSigns;
import java.util.List;

public interface VitalSignsService {

    /**
     * Save vital signs data for a patient
     * @param dto Vital signs data from frontend
     * @param patientId Patient's ID
     * @return Saved VitalSigns entity
     */
    VitalSigns saveVitalSigns(VitalSignsDTO dto, Long patientId);

    /**
     * Get all vital signs for a patient
     * @param patientId Patient's ID
     * @return List of vital signs ordered by date descending
     */
    List<VitalSigns> getPatientVitalSigns(Long patientId);

    /**
     * Get the most recent vital signs for a patient
     * @param patientId Patient's ID
     * @return Latest VitalSigns entity or null
     */
    VitalSigns getLatestVitalSigns(Long patientId);

    /**
     * Get specific vital signs by ID
     * @param id Vital signs ID
     * @return VitalSigns entity
     */
    VitalSigns getVitalSignsById(Long id);

    /**
     * Delete vital signs by ID
     * @param id Vital signs ID
     */
    void deleteVitalSigns(Long id);
}