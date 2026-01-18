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
@RequestMapping("/api/reports")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam String reportName,
            @RequestParam MultipartFile file
    ) {
        logger.info("Upload request received - Report: {}, File: {}, Size: {}",
                reportName, file.getOriginalFilename(), file.getSize());

        try {
            service.uploadReport(reportName, file);

            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("reportName", reportName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> getAllReports() {
        logger.info("Fetching all reports");
        try {
            return ResponseEntity.ok(service.getAllReports());
        } catch (Exception e) {
            logger.error("Failed to fetch reports: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) {
        logger.info("Download request for report ID: {}", id);

        try {
            MedicalReport report = service.getReportEntity(id);
            byte[] data = service.downloadReport(id);

            // Extract just the filename from the full path
            String filename = new File(report.getFileName()).getName();

            logger.info("Sending file: {} ({} bytes)", filename, data.length);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", report.getFileType())
                    .body(data);

        } catch (Exception e) {
            logger.error("Download failed for ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}