package com.example.vitalReports.logic;

import com.example.vitalReports.domain.enums.HealthStatus;
import com.example.vitalReports.domain.model.VitalReading;
import com.example.vitalReports.domain.model.VitalStatus;
import org.springframework.stereotype.Component;


@Component
public class RuleBasedVitalEvaluator implements VitalDecisionEngine {

    @Override
    public VitalStatus evaluate(VitalReading v) {
        return new VitalStatus(
                spo2(v.getSpo2()),
                pressure(v.getSystolicBP()),
                heartRate(v.getHeartRate()),
                temperature(v.getTemperature())
        );
    }

    private HealthStatus spo2(int value) {
        if (value >= 96) return HealthStatus.GOOD;
        if (value >= 94) return HealthStatus.AVERAGE;
        if (value >= 92) return HealthStatus.BAD;
        return HealthStatus.CRITICAL;
    }

    private HealthStatus pressure(int sbp) {
        if (sbp >= 110 && sbp <= 130) return HealthStatus.GOOD;
        if (sbp >= 100) return HealthStatus.AVERAGE;
        if (sbp >= 90) return HealthStatus.BAD;
        return HealthStatus.CRITICAL;
    }

    private HealthStatus heartRate(int hr) {
        if (hr >= 60 && hr <= 100) return HealthStatus.GOOD;
        if (hr <= 110) return HealthStatus.AVERAGE;
        if (hr <= 130) return HealthStatus.BAD;
        return HealthStatus.CRITICAL;
    }

    private HealthStatus temperature(double t) {
        if (t >= 36.1 && t <= 37.5) return HealthStatus.GOOD;
        if (t <= 38.0) return HealthStatus.AVERAGE;
        if (t <= 39.0) return HealthStatus.BAD;
        return HealthStatus.CRITICAL;
    }
}
