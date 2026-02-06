package com.example.mainservice.controller;

import com.example.mainservice.dto.ReportResponseDTO;
import com.example.mainservice.entity.MedicalReport;
import com.example.mainservice.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    // ✅ React: POST /api/patients/{patientId}/reports/upload
    @PostMapping("/patients/{patientId}/reports/upload")
    public ResponseEntity<?> upload(
            @PathVariable Long patientId,
            @RequestParam String reportName,
            @RequestParam MultipartFile file
    ) {
        logger.info("Upload request patientId={} reportName={} file={}", patientId, reportName, file.getOriginalFilename());

        try {
            service.uploadReport(patientId, reportName, file);

            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("reportName", reportName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // ✅ React: GET /api/patients/{patientId}/reports
    @GetMapping("/patients/{patientId}/reports")
    public ResponseEntity<List<ReportResponseDTO>> getPatientReports(@PathVariable Long patientId) {
        try {
            return ResponseEntity.ok(service.getReportsByPatient(patientId));
        } catch (Exception e) {
            logger.error("Failed to fetch patient reports", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ React: GET /api/reports/{id}/download
    @GetMapping("/reports/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) {
        try {
            MedicalReport report = service.getReportEntity(id);
            byte[] data = service.downloadReport(id);

            String filename = new File(report.getFileName()).getName();

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", report.getFileType())
                    .body(data);

        } catch (Exception e) {
            logger.error("Download failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
