package com.example.mainservice.service;

import com.example.mainservice.dto.CriticalAlertDTO;
import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.dto.DoctorPortalPatientDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.entity.VitalSigns;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.repository.VitalSignsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepo doctorrepo;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepo patientRepo;
    private final VitalSignsRepository vitalSignsRepository;
    private final com.example.mainservice.repository.ECGReadingRepository ecgReadingRepository;

    public Doctor create(DoctorDTO doctor) {

        Doctor d = Doctor.builder()
                .name(doctor.getName())
                .email(doctor.getEmail())
                .nicNo(doctor.getNicNo())
                .doctorRegNo(doctor.getDoctorRegNo())
                .address(doctor.getAddress())
                .gender(doctor.getGender())
                .contactNo(doctor.getContactNo())
                .hospital(doctor.getHospital())
                .password(passwordEncoder.encode(doctor.getPassword()))
                .position(doctor.getPosition())
                .username(doctor.getUsername())
                .dateOfBirth(doctor.getDateOfBirth())
                .build();
        return doctorrepo.save(d);

    }

    public List<DoctorDTO> getDetails() {

        return doctorrepo.findAll().stream().map(d -> DoctorDTO.builder()
                .Id(d.getId())
                .name(d.getName())
                .email(d.getEmail())
                .nicNo(d.getNicNo())
                .doctorRegNo(d.getDoctorRegNo())
                .address(d.getAddress())
                .gender(d.getGender())
                .contactNo(d.getContactNo())
                .hospital(d.getHospital())
                .password(d.getPassword())
                .position(d.getPosition())
                .username(d.getUsername())
                .dateOfBirth(d.getDateOfBirth()).build()).toList();
    }

    public void deleteDoctor(Long Id) {

        doctorrepo.deleteById(Id);
    }

    public DoctorDTO updateDoctor(Long Id, DoctorDTO dto) {
        Doctor d = doctorrepo.findById(Id).orElseThrow();

        if (dto.getDoctorRegNo() != null)
            d.setDoctorRegNo(dto.getDoctorRegNo());
        if (dto.getEmail() != null)
            d.setEmail(dto.getEmail());
        if (dto.getDateOfBirth() != null)
            d.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getContactNo() != null)
            d.setContactNo(dto.getContactNo());
        if (dto.getAddress() != null)
            d.setAddress(dto.getAddress());
        if (dto.getGender() != null)
            d.setGender(dto.getGender());
        if (dto.getHospital() != null)
            d.setHospital(dto.getHospital());
        if (dto.getName() != null)
            d.setName(dto.getName());
        if (dto.getNicNo() != null)
            d.setNicNo(dto.getNicNo());
        if (dto.getPassword() != null)
            d.setPassword(dto.getPassword());
        if (dto.getPosition() != null)
            d.setPosition(dto.getPosition());
        if (dto.getUsername() != null)
            d.setUsername(dto.getUsername());

        Doctor updatedDoctor = doctorrepo.save(d);
        return convertToDTO(updatedDoctor);

    }

    private DoctorDTO convertToDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setDoctorRegNo(doctor.getDoctorRegNo());
        dto.setName(doctor.getName());
        dto.setEmail(doctor.getEmail());
        dto.setDateOfBirth(doctor.getDateOfBirth());
        dto.setContactNo(doctor.getContactNo());
        dto.setAddress(doctor.getAddress());
        dto.setGender(doctor.getGender());
        dto.setHospital(doctor.getHospital());
        dto.setNicNo(doctor.getNicNo());
        dto.setPassword(doctor.getPassword());
        dto.setPosition(doctor.getPosition());
        dto.setUsername(doctor.getUsername());

        return dto;
    }

    /**
     * Get all patients assigned to a specific doctor with their latest vital signs.
     * Only returns patients where assignedDoctorId matches the provided doctorId.
     *
     * @param doctorId The ID of the logged-in doctor
     * @return List of patients with vital signs data
     */
    public List<DoctorPortalPatientDTO> getAssignedPatients(Long doctorId) {
        List<Patient> patients = patientRepo.findByAssignedDoctorId(doctorId);

        return patients.stream().map(patient -> {
            // Get latest vital signs for patient
            VitalSigns latestVitals = vitalSignsRepository
                    .findFirstByPatientIdOrderByMeasurementDateTimeDesc(patient.getId());

            return buildDoctorPortalDTO(patient, latestVitals);
        }).collect(Collectors.toList());
    }

    /**
     * Build DoctorPortalPatientDTO from patient and vitals data
     */
    private DoctorPortalPatientDTO buildDoctorPortalDTO(Patient patient, VitalSigns vitals) {
        DoctorPortalPatientDTO.DoctorPortalPatientDTOBuilder builder = DoctorPortalPatientDTO.builder()
                .patientId(patient.getId())
                .patientName(patient.getName())
                .bloodType(patient.getBloodType())
                .contactNo(patient.getContactNo())
                .medicalConditions(patient.getMedicalConditions())
                .city(patient.getCity())
                .district(patient.getDistrict())
                .address(patient.getAddress())
                .gender(patient.getGender())
                .dateOfBirth(patient.getDateOfBirth());

        if (vitals != null) {
            builder.room(vitals.getRoom())
                    .heartRate(vitals.getHeartRate())
                    .temperature(vitals.getTemperature())
                    .spo2(vitals.getSpo2())
                    .bloodPressureSystolic(vitals.getBloodPressureSystolic())
                    .bloodPressureDiastolic(vitals.getBloodPressureDiastolic());

            // Calculate risk level based on vitals
            builder.riskLevel(calculateRiskLevel(vitals));
            builder.status(calculateStatus(vitals));
        } else {
            builder.riskLevel("UNKNOWN");
            builder.status("NO_DATA");
        }

        return builder.build();
    }

    /**
     * Calculate risk level based on vital signs
     */
    private String calculateRiskLevel(VitalSigns vitals) {
        int riskScore = 0;

        // Heart rate assessment
        if (vitals.getHeartRate() != null) {
            if (vitals.getHeartRate() < 50 || vitals.getHeartRate() > 120) {
                riskScore += 3;
            } else if (vitals.getHeartRate() < 60 || vitals.getHeartRate() > 100) {
                riskScore += 1;
            }
        }

        // SpO2 assessment
        if (vitals.getSpo2() != null) {
            if (vitals.getSpo2() < 90) {
                riskScore += 3;
            } else if (vitals.getSpo2() < 95) {
                riskScore += 2;
            }
        }

        // Temperature assessment
        if (vitals.getTemperature() != null) {
            if (vitals.getTemperature() > 39.0 || vitals.getTemperature() < 35.0) {
                riskScore += 3;
            } else if (vitals.getTemperature() > 38.0 || vitals.getTemperature() < 36.0) {
                riskScore += 1;
            }
        }

        // Blood pressure assessment
        if (vitals.getBloodPressureSystolic() != null) {
            if (vitals.getBloodPressureSystolic() > 180 || vitals.getBloodPressureSystolic() < 90) {
                riskScore += 3;
            } else if (vitals.getBloodPressureSystolic() > 140 || vitals.getBloodPressureSystolic() < 100) {
                riskScore += 1;
            }
        }

        if (riskScore >= 6)
            return "CRITICAL";
        if (riskScore >= 4)
            return "HIGH";
        if (riskScore >= 2)
            return "MEDIUM";
        return "LOW";
    }

    /**
     * Calculate patient status based on vitals
     */
    private String calculateStatus(VitalSigns vitals) {
        String riskLevel = calculateRiskLevel(vitals);
        switch (riskLevel) {
            case "CRITICAL":
                return "CRITICAL";
            case "HIGH":
                return "MONITORING";
            default:
                return "STABLE";
        }
    }

    /**
     * Generate critical alerts for a doctor's assigned patients.
     * Each abnormal vital sign generates a separate alert.
     */
    public List<CriticalAlertDTO> getCriticalAlerts(Long doctorId) {
        List<Patient> patients = patientRepo.findByAssignedDoctorId(doctorId);

        java.util.ArrayList<CriticalAlertDTO> alerts = new java.util.ArrayList<>();

        for (Patient patient : patients) {
            // ============================================
            // 1. Vital Signs Alerts (using VitalReports-AI)
            // ============================================
            VitalSigns vitals = vitalSignsRepository
                    .findFirstByPatientIdOrderByMeasurementDateTimeDesc(patient.getId());
            String roomVal = "Unknown";

            if (vitals != null) {
                roomVal = vitals.getRoom();
                // Use cached triage level if available; else fallback to old calculation
                String overallRisk = vitals.getTriageLevel() != null ? vitals.getTriageLevel()
                        : calculateRiskLevel(vitals);

                if ("EMERGENCY".equalsIgnoreCase(overallRisk) || "CRITICAL".equalsIgnoreCase(overallRisk)
                        || "HIGH".equalsIgnoreCase(overallRisk) || "MEDIUM".equalsIgnoreCase(overallRisk)
                        || "BAD".equalsIgnoreCase(overallRisk)) {

                    // -- SpO2 --
                    if ("CRITICAL".equalsIgnoreCase(vitals.getSpo2Status())
                            || "BAD".equalsIgnoreCase(vitals.getSpo2Status())) {
                        alerts.add(CriticalAlertDTO.builder()
                                .patientId(patient.getId()).patientName(patient.getName()).room(roomVal)
                                .alertTitle("SpO2 Alert: " + vitals.getSpo2Status())
                                .description("SpO2 level is " + vitals.getSpo2() + "%")
                                .severity("CRITICAL".equalsIgnoreCase(vitals.getSpo2Status()) ? "CRITICAL" : "HIGH")
                                .currentValue(vitals.getSpo2() + "%").normalRange("95-100%")
                                .recordedAt(vitals.getMeasurementDateTime()).build());
                    }

                    // -- Heart Rate --
                    if ("CRITICAL".equalsIgnoreCase(vitals.getHeartRateStatus())
                            || "BAD".equalsIgnoreCase(vitals.getHeartRateStatus())) {
                        alerts.add(CriticalAlertDTO.builder()
                                .patientId(patient.getId()).patientName(patient.getName()).room(roomVal)
                                .alertTitle("Heart Rate Alert: " + vitals.getHeartRateStatus())
                                .description("Heart Rate is " + vitals.getHeartRate() + " bpm")
                                .severity(
                                        "CRITICAL".equalsIgnoreCase(vitals.getHeartRateStatus()) ? "CRITICAL" : "HIGH")
                                .currentValue(vitals.getHeartRate() + " bpm").normalRange("60-100 bpm")
                                .recordedAt(vitals.getMeasurementDateTime()).build());
                    }

                    // -- Blood Pressure --
                    if ("CRITICAL".equalsIgnoreCase(vitals.getPressureStatus())
                            || "BAD".equalsIgnoreCase(vitals.getPressureStatus())) {
                        String bpVal = vitals.getBloodPressureSystolic() + "/"
                                + (vitals.getBloodPressureDiastolic() != null ? vitals.getBloodPressureDiastolic()
                                        : "?")
                                + " mmHg";
                        alerts.add(CriticalAlertDTO.builder()
                                .patientId(patient.getId()).patientName(patient.getName()).room(roomVal)
                                .alertTitle("Blood Pressure Alert: " + vitals.getPressureStatus())
                                .description("Blood Pressure is " + bpVal)
                                .severity("CRITICAL".equalsIgnoreCase(vitals.getPressureStatus()) ? "CRITICAL" : "HIGH")
                                .currentValue(bpVal).normalRange("90-120/60-80 mmHg")
                                .recordedAt(vitals.getMeasurementDateTime()).build());
                    }

                    // -- Temperature --
                    if ("CRITICAL".equalsIgnoreCase(vitals.getTemperatureStatus())
                            || "BAD".equalsIgnoreCase(vitals.getTemperatureStatus())) {
                        alerts.add(CriticalAlertDTO.builder()
                                .patientId(patient.getId()).patientName(patient.getName()).room(roomVal)
                                .alertTitle("Temperature Alert: " + vitals.getTemperatureStatus())
                                .description("Temperature is " + vitals.getTemperature() + " °C")
                                .severity("CRITICAL".equalsIgnoreCase(vitals.getTemperatureStatus()) ? "CRITICAL"
                                        : "HIGH")
                                .currentValue(vitals.getTemperature() + " °C").normalRange("36.1-37.2°C")
                                .recordedAt(vitals.getMeasurementDateTime()).build());
                    }
                } else if (vitals.getTriageLevel() == null) {
                    // Fallback for old data without AI statuses
                    if ("CRITICAL".equals(overallRisk) || "HIGH".equals(overallRisk)) {
                        alerts.add(CriticalAlertDTO.builder()
                                .patientId(patient.getId()).patientName(patient.getName()).room(roomVal)
                                .alertTitle("Abnormal Vitals Detected")
                                .description("Legacy fallback: Vitals triggered high risk.")
                                .severity(overallRisk)
                                .currentValue("Check Vitals Tab").normalRange("N/A")
                                .recordedAt(vitals.getMeasurementDateTime()).build());
                    }
                }
            } // End if vitals != null

            // ============================================
            // 2. ECG Alerts
            // ============================================
            com.example.mainservice.entity.ECGReading ecg = ecgReadingRepository
                    .findFirstByPatientIdOrderByRecordedAtDesc(patient.getId());
            
            // Debug logging to help identify why alerts might not be showing
            if (ecg != null) {
                System.out.println("[ECG-DEBUG] Patient " + patient.getId() + " (" + patient.getName()
                        + ") - Latest ECG: prediction='" + ecg.getPrediction()
                        + "', status='" + ecg.getStatus() + "', recordedAt=" + ecg.getRecordedAt());
            }

            if (ecg != null && ecg.getPrediction() != null && !"Normal".equalsIgnoreCase(ecg.getPrediction())) {
                // Determine severity based on prediction and probability
                String severity = "CRITICAL";
                String prediction = ecg.getPrediction();
                
                // Downgrade to HIGH for general abnormalities or specific common arrhythmias
                if ("Abnormal".equalsIgnoreCase(prediction) || 
                    "Abnormal".equalsIgnoreCase(ecg.getStatus()) ||
                    prediction.toLowerCase().contains("premature") ||
                    ecg.getProbability() < 0.70) {
                    severity = "HIGH";
                }

                alerts.add(CriticalAlertDTO.builder()
                        .patientId(patient.getId())
                        .patientName(patient.getName())
                        .room(roomVal != null ? roomVal : "Room Unknown")
                        .alertTitle("ECG Alert: " + prediction)
                        .description(ecg.getRationale() != null ? ecg.getRationale() : "Abnormal ECG pattern detected by AI.")
                        .severity(severity)
                        .currentValue("Confidence: " + String.format("%.1f", ecg.getProbability() * 100) + "%")
                        .normalRange("Normal Sinus Rhythm")
                        .recordedAt(ecg.getRecordedAt())
                        .build());
            }
        } // End of Patient loop Here

        // Sort: CRITICAL first, then HIGH, then MEDIUM
        alerts.sort((a, b) -> {
            int sevA = "CRITICAL".equals(a.getSeverity()) ? 3 : "HIGH".equals(a.getSeverity()) ? 2 : 1;
            int sevB = "CRITICAL".equals(b.getSeverity()) ? 3 : "HIGH".equals(b.getSeverity()) ? 2 : 1;
            if (sevA != sevB)
                return sevB - sevA;
            if (a.getRecordedAt() != null && b.getRecordedAt() != null)
                return b.getRecordedAt().compareTo(a.getRecordedAt());
            return 0;
        });

        return alerts;
    }

    /**
     * Get doctor by ID
     */
    public Doctor getDoctorById(Long id) {
        return doctorrepo.findById(id).orElse(null);
    }

    /**
     * Get doctor by username or email (fallback for JWT compatibility)
     */
    public Doctor getDoctorByUsername(String usernameOrEmail) {
        // First try by username
        return doctorrepo.findByUsername(usernameOrEmail)
                .orElseGet(() ->
                // Fallback: try by email if username lookup fails
                doctorrepo.findByEmail(usernameOrEmail).orElse(null));
    }

    /**
     * Get ECG history for all patients assigned to a doctor
     */
    public List<com.example.mainservice.entity.ECGReading> getDoctorPatientsECGHistory(Long doctorId) {
        return ecgReadingRepository.findByPatient_AssignedDoctorIdOrderByRecordedAtDesc(doctorId);
    }
}
