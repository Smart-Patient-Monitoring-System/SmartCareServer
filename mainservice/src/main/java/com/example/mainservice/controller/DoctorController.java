package com.example.mainservice.controller;

import com.example.mainservice.dto.CriticalAlertDTO;
import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.dto.DoctorPortalPatientDTO;
import com.example.mainservice.dto.ECGReadingDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.ECGReading;
import com.example.mainservice.entity.EmergencyAlert;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.ECGReadingRepository;
import com.example.mainservice.repository.EmergencyAlertRepository;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.service.DoctorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller // initialize as a controller
@RestController // initialize rest API s
@RequestMapping("/api/doctor")
public class DoctorController {
    @Autowired
    private DoctorService doctorservice;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private EmergencyAlertRepository emergencyAlertRepository;

    @Autowired
    private ECGReadingRepository ecgReadingRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createDoctor(@Valid @RequestBody DoctorDTO doctorDto) {
        try {
            Doctor doctor = doctorservice.create(doctorDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(doctor);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during doctor creation: " + e.getMessage()));
        }
    }

    @GetMapping("/get")
    public List<DoctorDTO> getAllDoctors(){

        return doctorservice.getDetails();
    }


    @DeleteMapping("/delete/{Id}")
    public String deleteDoctorByID(@PathVariable Long Id) {
        try {
            doctorservice.deleteDoctor(Id);
            return "deleted successfully!";
        } catch (RuntimeException e) {
            return "Delete Failed";
        }
    }

    @PutMapping("/update/{Id}")
    public ResponseEntity<DoctorDTO> updateDoctorByID(
            @PathVariable Long Id,
            @RequestBody DoctorDTO doctorDto) {
        try {
            DoctorDTO updatedDoctor = doctorservice.updateDoctor(Id, doctorDto);
            return ResponseEntity.ok(updatedDoctor);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Doctor Portal: Get all patients assigned to the logged-in doctor.
     * Returns patient data with latest vital signs, risk level, and status.
     *
     * @param principal The authenticated user (doctor)
     * @return List of assigned patients with vital signs data
     */
    @GetMapping("/my-patients")
    public ResponseEntity<?> getMyPatients(Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Not authenticated"));
            }

            // Get doctor by username from JWT/session
            Doctor doctor = doctorservice.getDoctorByUsername(principal.getName());
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Doctor not found"));
            }

            List<DoctorPortalPatientDTO> patients = doctorservice.getAssignedPatients(doctor.getId());
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching patients: " + e.getMessage()));
        }
    }

    /**
     * Doctor Portal: Get assigned patients by doctor ID (for testing/admin use).
     *
     * @param doctorId The doctor's ID
     * @return List of assigned patients with vital signs data
     */
    @GetMapping("/patients/{doctorId}")
    public ResponseEntity<?> getPatientsByDoctorId(@PathVariable Long doctorId) {
        try {
            Doctor doctor = doctorservice.getDoctorById(doctorId);
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Doctor not found"));
            }

            List<DoctorPortalPatientDTO> patients = doctorservice.getAssignedPatients(doctorId);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching patients: " + e.getMessage()));
        }
    }

    /**
     * Doctor Portal: Get emergency alerts for all patients assigned to a doctor.
     *
     * @param doctorId The doctor's ID
     * @return List of emergency alerts for the doctor's assigned patients
     */
    @GetMapping("/alerts/{doctorId}")
    public ResponseEntity<?> getAlertsForDoctor(@PathVariable Long doctorId) {
        try {
            Doctor doctor = doctorservice.getDoctorById(doctorId);
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Doctor not found"));
            }

            // Get all patients assigned to this doctor
            List<Patient> assignedPatients = patientRepo.findByAssignedDoctorId(doctorId);

            // Collect alerts for all assigned patients
            List<EmergencyAlert> alerts = assignedPatients.stream()
                    .flatMap(patient -> emergencyAlertRepository.findByUserId(patient.getId()).stream())
                    .sorted((a, b) -> {
                        if (a.getCreatedAt() == null || b.getCreatedAt() == null)
                            return 0;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    })
                    .toList();

            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching alerts: " + e.getMessage()));
        }
    }

    /**
     * Doctor Portal: Get real-time critical alerts based on patients' abnormal
     * vital signs.
     * Each abnormal vital (HR, SpO2, Temp, BP) generates a separate alert.
     *
     * @param doctorId The doctor's ID
     * @return List of critical alerts sorted by severity
     */
    @GetMapping("/critical-alerts/{doctorId}")
    public ResponseEntity<?> getCriticalAlertsByDoctorId(@PathVariable Long doctorId) {
        try {
            Doctor doctor = doctorservice.getDoctorById(doctorId);
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Doctor not found"));
            }

            List<CriticalAlertDTO> alerts = doctorservice.getCriticalAlerts(doctorId);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching critical alerts: " + e.getMessage()));
        }
    }

    /**
     * Doctor Portal: Get ECG history for all patients assigned to a doctor.
     *
     * @param doctorId The doctor's ID
     * @return List of historical ECG readings
     */
    @GetMapping("/{doctorId}/patients/ecg-history")
    public ResponseEntity<?> getDoctorPatientsECGHistory(@PathVariable Long doctorId) {
        try {
            Doctor doctor = doctorservice.getDoctorById(doctorId);
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Doctor not found"));
            }

            List<com.example.mainservice.entity.ECGReading> history = doctorservice.getDoctorPatientsECGHistory(doctorId);
            List<ECGReadingDTO> dtoList = history.stream().map(h -> ECGReadingDTO.builder()
                    .id(h.getId())
                    .patientId(h.getPatient().getId())
                    .patientName(h.getPatient().getName())
                    .prediction(h.getPrediction())
                    .probability(h.getProbability())
                    .meanHR(h.getMeanHR())
                    .sdnn(h.getSdnn())
                    .rmssd(h.getRmssd())
                    .beats(h.getBeats())
                    .status(h.getStatus())
                    .rationale(h.getRationale())
                    .waveformJson(h.getWaveformJson())
                    .recordedAt(h.getRecordedAt())
                    .build()).collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching ECG history: " + e.getMessage()));
        }
    }

    /**
     * Doctor Portal: Save an ECG Reading from VitalReports-AI directly to the
     * database.
     */
    @PostMapping("/ecg/save")
    public ResponseEntity<?> saveECGReading(@RequestBody ECGReadingDTO dto) {
        try {
            System.out.println("[ECG-SAVE] Received save request — patientId=" + dto.getPatientId()
                    + ", prediction='" + dto.getPrediction() + "', status='" + dto.getStatus()
                    + "', probability=" + dto.getProbability());

            Patient patient = patientRepo.findById(dto.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + dto.getPatientId()));

            ECGReading reading = ECGReading.builder()
                    .patient(patient)
                    .prediction(dto.getPrediction())
                    .probability(dto.getProbability())
                    .meanHR(dto.getMeanHR())
                    .sdnn(dto.getSdnn())
                    .rmssd(dto.getRmssd())
                    .beats(dto.getBeats())
                    .status(dto.getStatus())
                    .rationale(dto.getRationale())
                    .waveformJson(dto.getWaveformJson())
                    .build();

            ECGReading saved = ecgReadingRepository.save(reading);
            System.out.println("[ECG-SAVE] SUCCESS — Saved ECG reading id=" + saved.getId()
                    + " for patient " + patient.getName() + " (id=" + patient.getId() + ")"
                    + ", prediction='" + saved.getPrediction() + "'"
                    + ", recordedAt=" + saved.getRecordedAt());

            return ResponseEntity.ok(Map.of("message", "ECG reading saved successfully", "id", saved.getId()));
        } catch (Exception e) {
            System.err.println("[ECG-SAVE] FAILED: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String errorMessage = errors.values().stream()
                .findFirst()
                .orElse("Validation failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage));
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }

}
