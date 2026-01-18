package com.example.vitalReports.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.example.vitalReports.domain.model.VitalAssessment;
import com.example.vitalReports.domain.model.VitalReading;
import com.example.vitalReports.service.VitalProcessingService;

@RestController
@RequestMapping("/api/vitals")
@CrossOrigin
public class VitalController {

    private final VitalProcessingService processingService;

    public VitalController(VitalProcessingService processingService) {
        this.processingService = processingService;
    }

    @PostMapping("/evaluate")
    public VitalAssessment evaluateVitals(
            @Valid @RequestBody VitalReading reading
    ) {
        return processingService.process(reading);
    }
}
