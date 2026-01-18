package com.example.mainservice.service.impl;

import com.example.mainservice.dto.ReportResponseDTO;
import com.example.mainservice.entity.MedicalReport;
import com.example.mainservice.repository.MedicalReportRepository;
import com.example.mainservice.service.ReportService;
import com.example.mainservice.util.FileStorageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    private final MedicalReportRepository repository;

    public ReportServiceImpl(MedicalReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public void uploadReport(String reportName, MultipartFile file) {
        logger.info("Attempting to upload report: {} (size: {} bytes)", reportName, file.getSize());

        try {
            // Validate inputs
            if (reportName == null || reportName.trim().isEmpty()) {
                throw new IllegalArgumentException("Report name cannot be empty");
            }

            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            // Save file
            String filePath = FileStorageUtil.saveFile(file);
            logger.info("File saved to: {}", filePath);

            // Create and save entity
            MedicalReport report = new MedicalReport(
                    reportName,
                    filePath,
                    file.getContentType(),
                    file.getSize()
            );

            repository.save(report);
            logger.info("Report saved to database with ID: {}", report.getId());

        } catch (IOException e) {
            logger.error("Failed to save file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during upload: {}", e.getMessage(), e);
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ReportResponseDTO> getAllReports() {
        logger.info("Fetching all reports");
        return repository.findAll()
                .stream()
                .map(r -> new ReportResponseDTO(
                        r.getId(),
                        r.getReportName(),
                        r.getUploadedAt()
                ))
                .toList();
    }

    @Override
    public byte[] downloadReport(Long id) {
        logger.info("Attempting to download report ID: {}", id);

        try {
            MedicalReport report = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));

            Path filePath = Paths.get(report.getFileName());

            if (!Files.exists(filePath)) {
                logger.error("File not found at path: {}", filePath);
                throw new RuntimeException("File not found on disk: " + report.getFileName());
            }

            byte[] data = Files.readAllBytes(filePath);
            logger.info("Successfully read {} bytes from file", data.length);
            return data;

        } catch (IOException e) {
            logger.error("Failed to read file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }

    @Override
    public MedicalReport getReportEntity(Long id) {
        logger.info("Fetching report entity ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));
    }
}