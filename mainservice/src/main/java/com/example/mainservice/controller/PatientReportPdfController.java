package com.example.mainservice.controller;

import com.example.mainservice.service.PatientReportPdfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients/{patientId}")
public class PatientReportPdfController {

    private final PatientReportPdfService pdfService;

    public PatientReportPdfController(PatientReportPdfService pdfService) {
        this.pdfService = pdfService;
    }

    //  React: GET /api/patients/{patientId}/report.pdf?from=...&to=...
    @GetMapping("/report.pdf")
    public ResponseEntity<byte[]> generatePdf(
            @PathVariable Long patientId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        byte[] pdf = pdfService.generatePdf(patientId, from, to);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"patient_" + patientId + "_report.pdf\"")
                .body(pdf);
    }
}
