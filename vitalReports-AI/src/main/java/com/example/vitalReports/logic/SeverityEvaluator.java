package com.example.vitalReports.logic;

import com.example.vitalReports.domain.enums.HealthStatus;
import com.example.vitalReports.domain.enums.TriageLevel;
import com.example.vitalReports.domain.model.VitalStatus;
import org.springframework.stereotype.Component;

@Component
public class SeverityEvaluator {

    public TriageLevel evaluate(VitalStatus status) {

        int criticalCount = count(status, HealthStatus.CRITICAL);
        int badCount = count(status, HealthStatus.BAD);

        // 🚨 Any CRITICAL → EMERGENCY
        if (criticalCount >= 1) {
            return TriageLevel.EMERGENCY;
        }

        // ⚠️ Multiple BAD → HIGH
        if (badCount >= 2) {
            return TriageLevel.HIGH;
        }

        // ⚠️ One BAD → MEDIUM
        if (badCount == 1) {
            return TriageLevel.MEDIUM;
        }

        // ✅ Otherwise safe
        return TriageLevel.LOW;
    }

    private int count(VitalStatus status, HealthStatus target) {
        int count = 0;

        if (status.getSpo2Status() == target) count++;
        if (status.getPressureStatus() == target) count++;
        if (status.getHeartRateStatus() == target) count++;
        if (status.getTemperatureStatus() == target) count++;

        return count;
    }
}
