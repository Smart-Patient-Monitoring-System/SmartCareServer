package com.example.vitalReports.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.vitalReports.domain.model.ECGPredictionResponse;
import com.example.vitalReports.service.ECGPredictionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vital/ecg")
public class ECGController {

    private final ECGPredictionService ecgPredictionService;

    public ECGController(ECGPredictionService ecgPredictionService) {
        this.ecgPredictionService = ecgPredictionService;
    }

    /**
     * File upload endpoint: accepts .dat + .hea files for full ECG analysis.
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeECG(
            @RequestParam("dat_file") MultipartFile datFile,
            @RequestParam("hea_file") MultipartFile heaFile) {

        if (datFile.isEmpty() || heaFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Both .dat and .hea files are required"));
        }

        try {
            ECGPredictionResponse result = ecgPredictionService.analyzeFiles(datFile, heaFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("[ECG ERROR] " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "ECG analysis failed: " + e.getMessage()));
        }
    }

    /**
     * Signal-based endpoint: accepts a 180-sample signal array.
     */
    @PostMapping("/predict")
    public ResponseEntity<?> predictECG(
            @RequestBody Map<String, List<Double>> body) {

        List<Double> signal = body.get("signal");

        if (signal == null || signal.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Signal array is required"));
        }

        try {
            ECGPredictionResponse result = ecgPredictionService.predictECG(signal);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("[ECG ERROR] " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "ECG prediction failed: " + e.getMessage()));
        }
    }
}
