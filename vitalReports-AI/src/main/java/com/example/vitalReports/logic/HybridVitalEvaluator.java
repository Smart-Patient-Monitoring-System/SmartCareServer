package com.example.vitalReports.logic;

import com.example.vitalReports.domain.enums.HealthStatus;
import com.example.vitalReports.domain.model.VitalReading;
import com.example.vitalReports.domain.model.VitalStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class HybridVitalEvaluator implements VitalDecisionEngine {

    private final RuleBasedVitalEvaluator ruleEngine;
    private final AIVitalEvaluator aiEngine;

    public HybridVitalEvaluator(RuleBasedVitalEvaluator ruleEngine,
                                AIVitalEvaluator aiEngine) {
        this.ruleEngine = ruleEngine;
        this.aiEngine = aiEngine;
    }

    @Override
    public VitalStatus evaluate(VitalReading reading) {

        // 1️⃣ Always evaluate using rules (safety net)
        VitalStatus ruleResult = ruleEngine.evaluate(reading);

        // 2️⃣ If any CRITICAL detected → TRUST RULES
        if (hasCritical(ruleResult)) {
            return ruleResult;
        }

        // 3️⃣ Otherwise, use AI result
        return aiEngine.evaluate(reading);
    }

    private boolean hasCritical(VitalStatus status) {
        return status.getSpo2Status() == HealthStatus.CRITICAL
                || status.getPressureStatus() == HealthStatus.CRITICAL
                || status.getHeartRateStatus() == HealthStatus.CRITICAL
                || status.getTemperatureStatus() == HealthStatus.CRITICAL;
    }
}
