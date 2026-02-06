package com.example.mainservice.service;

public interface PatientReportPdfService {
    byte[] generatePdf(Long patientId, String from, String to);
}
