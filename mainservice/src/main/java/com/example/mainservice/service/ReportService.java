package com.example.mainservice.service;

import com.example.mainservice.dto.ReportResponseDTO;
import com.example.mainservice.entity.MedicalReport;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReportService {

    void uploadReport(Long patientId, String reportName, MultipartFile file);

    List<ReportResponseDTO> getReportsByPatient(Long patientId);

    byte[] downloadReport(Long id);

    MedicalReport getReportEntity(Long id);

    List<MedicalReport> getReportEntitiesByPatient(Long patientId);

}
