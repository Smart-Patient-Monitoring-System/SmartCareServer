package com.example.mainservice.service.impl;

import com.example.mainservice.dto.MedicalEventResponseDTO;
import com.example.mainservice.dto.MedicalSummaryDTO;
import com.example.mainservice.entity.MedicalReport;
import com.example.mainservice.entity.Patient;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.service.MedicalEventService;
import com.example.mainservice.service.PatientReportPdfService;
import com.example.mainservice.service.ReportService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PatientReportPdfServiceImpl implements PatientReportPdfService {

    private final PatientRepo patientRepo;
    private final MedicalEventService medicalEventService;
    private final ReportService reportService;

    public PatientReportPdfServiceImpl(
            PatientRepo patientRepo,
            MedicalEventService medicalEventService,
            ReportService reportService
    ) {
        this.patientRepo = patientRepo;
        this.medicalEventService = medicalEventService;
        this.reportService = reportService;
    }

    @Override
    public byte[] generatePdf(Long patientId, String from, String to) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        MedicalSummaryDTO summary = medicalEventService.getSummary(patientId);
        List<MedicalEventResponseDTO> events = medicalEventService.getEvents(patientId, from, to);

        // ✅ Use FULL report entities (with OCR text)
        List<MedicalReport> reportsFull = reportService.getReportEntitiesByPatient(patientId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);

            doc.open();

            Font title = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font h = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL);

            doc.add(new Paragraph("Patient Medical Report", title));
            doc.add(new Paragraph("Generated at: " + LocalDateTime.now(), normal));
            doc.add(Chunk.NEWLINE);

            /* -------------------- Patient Info -------------------- */
            doc.add(new Paragraph("Patient Information", h));
            doc.add(new Paragraph("Name: " + safe(patient.getName()), normal));
            doc.add(new Paragraph("Email: " + safe(patient.getEmail()), normal));
            doc.add(new Paragraph("Contact: " + safe(patient.getContactNo()), normal));
            doc.add(new Paragraph("NIC: " + safe(patient.getNicNo()), normal));
            doc.add(new Paragraph("DOB: " + (patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : "N/A"), normal));
            doc.add(Chunk.NEWLINE);

            /* -------------------- Summary -------------------- */
            doc.add(new Paragraph("Current Medical Summary", h));

            if (summary.getLatestVitals() != null) {
                Map<String, Object> v = summary.getLatestVitals();
                doc.add(new Paragraph("Latest Vitals (recordedAt: " + safeObj(v.get("recordedAt")) + ")", normal));
                doc.add(new Paragraph(
                        "BP: " + safeObj(v.get("bp")) +
                                ", SpO2: " + safeObj(v.get("spo2")) +
                                ", Temp: " + safeObj(v.get("temp")) +
                                ", HR: " + safeObj(v.get("heartRate")),
                        normal
                ));
            } else {
                doc.add(new Paragraph("Latest Vitals: N/A", normal));
            }

            doc.add(new Paragraph("Active Medications: " + (summary.getActiveMedications() != null ? summary.getActiveMedications().size() : 0), normal));
            doc.add(new Paragraph("Active Diagnoses: " + (summary.getActiveDiagnoses() != null ? summary.getActiveDiagnoses().size() : 0), normal));
            doc.add(new Paragraph("Allergies: " + (summary.getAllergies() != null ? summary.getAllergies().size() : 0), normal));
            doc.add(Chunk.NEWLINE);

            /* -------------------- Timeline -------------------- */
            doc.add(new Paragraph("Medical Events Timeline", h));

            if (events.isEmpty()) {
                doc.add(new Paragraph("No events found for selected range.", normal));
            } else {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.addCell("Recorded At");
                table.addCell("Type");
                table.addCell("Details");

                for (MedicalEventResponseDTO e : events) {
                    table.addCell(e.getRecordedAt() != null ? e.getRecordedAt().toString() : "N/A");
                    table.addCell(safe(e.getType()));
                    table.addCell(payloadToText(e.getPayload()));
                }
                doc.add(table);
            }

            doc.add(Chunk.NEWLINE);

            /* -------------------- Uploaded Reports + OCR Text -------------------- */
            doc.add(new Paragraph("Uploaded Reports (with Extracted Text)", h));

            if (reportsFull.isEmpty()) {
                doc.add(new Paragraph("No uploaded reports.", normal));
            } else {
                for (MedicalReport r : reportsFull) {

                    // Report heading line
                    doc.add(new Paragraph(
                            "- " + safe(r.getReportName()) +
                                    " (uploadedAt: " + (r.getUploadedAt() != null ? r.getUploadedAt().toString() : "N/A") + ")",
                            normal
                    ));

                    // OCR status
                    doc.add(new Paragraph("OCR Status: " + safe(r.getOcrStatus()), normal));

                    // OCR text
                    if ("DONE".equalsIgnoreCase(r.getOcrStatus())
                            && r.getExtractedText() != null
                            && !r.getExtractedText().isBlank()) {

                        doc.add(new Paragraph("Extracted Text:", h));
                        doc.add(new Paragraph(r.getExtractedText(), normal));

                    } else if ("FAILED".equalsIgnoreCase(r.getOcrStatus())) {

                        doc.add(new Paragraph("OCR Failed: " + safe(r.getOcrError()), normal));

                    } else {
                        doc.add(new Paragraph("No text available.", normal));
                    }

                    doc.add(Chunk.NEWLINE);
                }
            }

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    private String payloadToText(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) return "N/A";
        StringBuilder sb = new StringBuilder();
        payload.forEach((k, v) -> sb.append(k).append("=").append(v).append(", "));
        if (sb.length() >= 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "N/A" : s;
    }

    private String safeObj(Object o) {
        if (o == null) return "N/A";
        String s = String.valueOf(o);
        return s.isBlank() ? "N/A" : s;
    }
}
