package com.example.mainservice.service.impl;

import com.example.mainservice.dto.ReportResponseDTO;
import com.example.mainservice.entity.MedicalReport;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.MedicalReportRepository;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.service.OcrExtractionService;
import com.example.mainservice.service.ReportService;
import com.example.mainservice.util.FileStorageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final MedicalReportRepository reportRepository;
    private final PatientRepo patientRepo;
    private final OcrExtractionService ocrService;

    public ReportServiceImpl(MedicalReportRepository reportRepository, PatientRepo patientRepo,OcrExtractionService ocrService) {
        this.reportRepository = reportRepository;
        this.patientRepo = patientRepo;
        this.ocrService = ocrService;
    }

    @Override
    public List<MedicalReport> getReportEntitiesByPatient(Long patientId) {
        return reportRepository.findByPatient_IdOrderByUploadedAtDesc(patientId);
    }


    @Override
    public void uploadReport(Long patientId, String reportName, MultipartFile file) {
        logger.info("Upload report for patientId={}, reportName={}, file={}", patientId, reportName, file.getOriginalFilename());

        if (patientId == null) throw new IllegalArgumentException("patientId is required");
        if (reportName == null || reportName.trim().isEmpty()) throw new IllegalArgumentException("Report name cannot be empty");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File cannot be empty");

        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));

        try {
            String filePath = FileStorageUtil.saveFile(file);

            MedicalReport report = new MedicalReport(
                    reportName.trim(),
                    filePath,
                    file.getContentType(),
                    file.getSize(),
                    patient
            );

            report.setOcrStatus("PENDING");
            reportRepository.save(report);
            try {
                String text = ocrService.extractText(Path.of(filePath), file.getContentType());
                report.setExtractedText(text);
                report.setOcrStatus("DONE");
                report.setOcrError(null);
            } catch (Exception ex) {
                report.setOcrStatus("FAILED");
                report.setOcrError(ex.getMessage());
            }
            reportRepository.save(report);
            logger.info("Saved report id={} for patientId={}", report.getId(), patientId);

        } catch (IOException e) {
            logger.error("Failed to save file", e);
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ReportResponseDTO> getReportsByPatient(Long patientId) {
        if (patientId == null) throw new IllegalArgumentException("patientId is required");
        return reportRepository.findByPatient_IdOrderByUploadedAtDesc(patientId)
                .stream()
                .map(r -> new ReportResponseDTO(r.getId(), r.getReportName(), r.getUploadedAt()))
                .toList();
    }

    @Override
    public byte[] downloadReport(Long id) {
        MedicalReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));

        try {
            Path filePath = Paths.get(report.getFileName());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File not found on disk: " + report.getFileName());
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }

    @Override
    public MedicalReport getReportEntity(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));
    }
}
