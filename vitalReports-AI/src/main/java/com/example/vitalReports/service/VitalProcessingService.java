package com.example.vitalReports.service;

import com.example.vitalReports.domain.enums.TriageLevel;
import com.example.vitalReports.domain.model.VitalAssessment;
import com.example.vitalReports.domain.model.VitalReading;
import com.example.vitalReports.domain.model.VitalStatus;
import com.example.vitalReports.logic.SeverityEvaluator;
import com.example.vitalReports.logic.VitalDecisionEngine;
import org.springframework.stereotype.Service;

@Service
public class VitalProcessingService {

    private final VitalDecisionEngine decisionEngine;
    private final SeverityEvaluator severityEvaluator;

    public VitalProcessingService(
            VitalDecisionEngine decisionEngine,
            SeverityEvaluator severityEvaluator
    ) {
        this.decisionEngine = decisionEngine;
        this.severityEvaluator = severityEvaluator;
    }

    public VitalAssessment process(VitalReading reading) {

        VitalStatus status = decisionEngine.evaluate(reading);
        TriageLevel level = severityEvaluator.evaluate(status);

        return new VitalAssessment(status, level);
    }
}
