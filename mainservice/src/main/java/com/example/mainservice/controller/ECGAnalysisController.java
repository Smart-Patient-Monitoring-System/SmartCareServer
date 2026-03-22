package com.example.mainservice.controller;

import com.example.mainservice.dto.ECGAnalysisResponseDTO;
import com.example.mainservice.service.ECGAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/vital/ecg")
@RequiredArgsConstructor
@Slf4j
public class ECGAnalysisController {

    private final ECGAnalysisService ecgAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(
            @RequestParam("patientId") String patientId,
            @RequestParam("dat_file") MultipartFile datFile,
            @RequestParam("hea_file") MultipartFile heaFile) {
        try {
            log.info("Analyzing ECG upload for patient {}", patientId);
            ECGAnalysisResponseDTO response = ecgAnalysisService.analyze(datFile, heaFile);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("ECG analysis failed for patient {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to analyze ECG upload"));
        }
    }
}
