package com.example.mainservice.dto;

import java.util.List;
import java.util.Map;

public class MedicalSummaryDTO {

    private Map<String, Object> latestVitals; // includes bp/spo2/temp/heartRate + recordedAt
    private List<Map<String, Object>> activeMedications;
    private List<Map<String, Object>> activeDiagnoses;
    private List<Map<String, Object>> allergies;

    public MedicalSummaryDTO(
            Map<String, Object> latestVitals,
            List<Map<String, Object>> activeMedications,
            List<Map<String, Object>> activeDiagnoses,
            List<Map<String, Object>> allergies
    ) {
        this.latestVitals = latestVitals;
        this.activeMedications = activeMedications;
        this.activeDiagnoses = activeDiagnoses;
        this.allergies = allergies;
    }

    public Map<String, Object> getLatestVitals() { return latestVitals; }
    public List<Map<String, Object>> getActiveMedications() { return activeMedications; }
    public List<Map<String, Object>> getActiveDiagnoses() { return activeDiagnoses; }
    public List<Map<String, Object>> getAllergies() { return allergies; }
}
