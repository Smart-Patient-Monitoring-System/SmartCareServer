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
import com.example.mainservice.client.VitalReportsClient;
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

@Controller
@RestController
@RequestMapping("/api/doctor")
public class DoctorController {
    private static final int ECG_RATIONALE_DB_SAFE_LENGTH = 250;

    @Autowired
    private DoctorService doctorservice;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private EmergencyAlertRepository emergencyAlertRepository;

    @Autowired
    private ECGReadingRepository ecgReadingRepository;

    @Autowired
    private VitalReportsClient vitalReportsClient;

    @PostMapping("/create")
    public ResponseEntity<?> createDoctor(@Valid @RequestBody DoctorDTO doctorDto) {
        try {
            Doctor doctor = doctorservice.create(doctorDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(doctor);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during doctor creation: " + e.getMessage()));
        }
    }

    @GetMapping("/get")
    public List<DoctorDTO> getAllDocters() {
        return doctorservice.getDetails();
    }

    @GetMapping("/my-patients")
    public ResponseEntity<?> getMyPatients(Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Not authenticated"));
            }

            Doctor doctor = doctorservice.getDoctorByUsername(principal.getName());
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Doctor not found"));
            }

            List<DoctorPortalPatientDTO> patients = doctorservice.getAssignedPatients(doctor.getId());
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching patients: " + e.getMessage()));
        }
    }

    @GetMapping("/patients/{doctorId}")
    public ResponseEntity<?> getPatientsByDoctorId(@PathVariable Long doctorId) {
        try {
            Doctor doctor = doctorservice.getDoctorById(doctorId);
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Doctor not found"));
            }

            List<DoctorPortalPatientDTO> patients = doctorservice.getAssignedPatients(doctorId);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching patients: " + e.getMessage()));
        }
    }

    @GetMapping("/alerts/{doctorId}")
    public ResponseEntity<?> getAlertsForDoctor(@PathVariable Long doctorId) {
        try {
            Doctor doctor = doctorservice.getDoctorById(doctorId);
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Doctor not found"));
            }

            List<Patient> assignedPatients = patientRepo.findByAssignedDoctorId(doctorId);
            List<EmergencyAlert> alerts = assignedPatients.stream()
                    .flatMap(patient -> emergencyAlertRepository.findByUserId(patient.getId()).stream())
                    .sorted((a, b) -> {
                        if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    })
                    .toList();

            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching alerts: " + e.getMessage()));
        }
    }

    @GetMapping("/critical-alerts/{doctorId}")
    public ResponseEntity<?> getCriticalAlertsByDoctorId(@PathVariable Long doctorId) {
        try {
            Doctor doctor = doctorservice.getDoctorById(doctorId);
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Doctor not found"));
            }

            List<CriticalAlertDTO> alerts = doctorservice.getCriticalAlerts(doctorId);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching critical alerts: " + e.getMessage()));
        }
    }

    @GetMapping("/{doctorId}/patients/ecg-history")
    public ResponseEntity<?> getDoctorPatientsECGHistory(@PathVariable Long doctorId) {
        try {
            Doctor doctor = doctorservice.getDoctorById(doctorId);
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Doctor not found"));
            }

            List<ECGReadingDTO> dtoList = doctorservice.getDoctorPatientsECGHistory(doctorId).stream()
                    .map(h -> ECGReadingDTO.builder()
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
                            .build())
                    .toList();

            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching ECG history: " + e.getMessage()));
        }
    }

    @PostMapping("/ecg/save")
    public ResponseEntity<?> saveECGReading(@RequestBody ECGReadingDTO dto) {
        try {
            Patient patient = patientRepo.findById(dto.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + dto.getPatientId()));

            String safeRationale = dto.getRationale();
            if (safeRationale != null && safeRationale.length() > ECG_RATIONALE_DB_SAFE_LENGTH) {
                safeRationale = safeRationale.substring(0, ECG_RATIONALE_DB_SAFE_LENGTH);
            }

            ECGReading saved = ecgReadingRepository.save(ECGReading.builder()
                    .patient(patient)
                    .prediction(dto.getPrediction())
                    .probability(dto.getProbability())
                    .meanHR(dto.getMeanHR())
                    .sdnn(dto.getSdnn())
                    .rmssd(dto.getRmssd())
                    .beats(dto.getBeats())
                    .status(dto.getStatus())
                    .rationale(safeRationale)
                    .waveformJson(dto.getWaveformJson())
                    .build());

            return ResponseEntity.ok(Map.of("message", "ECG reading saved successfully", "id", saved.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/ecg/analyze-and-save/{patientId}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeAndSaveECG(
            @PathVariable Long patientId,
            @RequestParam("dat_file") org.springframework.web.multipart.MultipartFile datFile,
            @RequestParam("hea_file") org.springframework.web.multipart.MultipartFile heaFile) {
        try {
            Patient patient = patientRepo.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));

            Map<String, Object> aiResult = vitalReportsClient.analyzeECG(String.valueOf(patientId), datFile, heaFile);
            
            if (aiResult == null || aiResult.containsKey("error")) {
                throw new RuntimeException("VitalReports-AI Error: " + (aiResult != null ? aiResult.get("error") : "Null response"));
            }

            String prediction = (String) aiResult.getOrDefault("prediction", aiResult.getOrDefault("status", "Unknown"));
            
            double probability = parseDouble(aiResult.get("probability"));
            int meanHR = (int) parseDouble(aiResult.get("meanHR"));
            double sdnn = parseDouble(aiResult.get("SDNN") != null ? aiResult.get("SDNN") : aiResult.get("sdnn"));
            double rmssd = parseDouble(aiResult.get("RMSSD") != null ? aiResult.get("RMSSD") : aiResult.get("rmssd"));
            int beats = (int) parseDouble(aiResult.get("beats"));
            
            String status = (String) aiResult.getOrDefault("status", "Unknown");
            String rationaleRaw = (String) aiResult.get("rationale");
            String safeRationale = rationaleRaw != null ? rationaleRaw : "No rationale provided.";
            if (safeRationale.length() > ECG_RATIONALE_DB_SAFE_LENGTH) {
                safeRationale = safeRationale.substring(0, ECG_RATIONALE_DB_SAFE_LENGTH);
            }

            Object waveformObj = aiResult.get("waveform");
            String waveformJson = "[]";
            if (waveformObj != null) {
                waveformJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(waveformObj);
            }

            ECGReading saved = ecgReadingRepository.save(ECGReading.builder()
                    .patient(patient)
                    .prediction(prediction)
                    .probability(probability)
                    .meanHR(meanHR)
                    .sdnn(sdnn)
                    .rmssd(rmssd)
                    .beats(beats)
                    .status(status)
                    .rationale(safeRationale)
                    .waveformJson(waveformJson)
                    .build());

            Map<String, Object> response = new HashMap<>(aiResult);
            response.put("id", saved.getId());
            response.put("message", "ECG reading analyzed and saved successfully via S2S");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
    
    private double parseDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
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
    public ResponseEntity<DoctorDTO> updateDoctorByID(@PathVariable Long Id, @RequestBody DoctorDTO doctorDto) {
        try {
            DoctorDTO updatedDoctor = doctorservice.updateDoctor(Id, doctorDto);
            return ResponseEntity.ok(updatedDoctor);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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

        String errorMessage = errors.values().stream().findFirst().orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errorMessage));
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }
}
