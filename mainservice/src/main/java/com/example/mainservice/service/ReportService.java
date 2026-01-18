package com.example.mainservice.service;

import com.example.mainservice.dto.ReportResponseDTO;
import com.example.mainservice.entity.MedicalReport;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReportService {

    void uploadReport(String reportName, MultipartFile file);

    List<ReportResponseDTO> getAllReports();

    byte[] downloadReport(Long id);

    MedicalReport getReportEntity(Long id); // new helper
}
