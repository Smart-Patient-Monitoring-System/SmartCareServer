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
                temperature(v.getTemperature()),
                sugar(v.getBloodSugar()));
    }

    private HealthStatus spo2(int value) {
        if (value <= 0)
            return HealthStatus.GOOD; // No data
        if (value >= 96)
            return HealthStatus.GOOD;
        if (value >= 94)
            return HealthStatus.AVERAGE;
        if (value >= 92)
            return HealthStatus.BAD;
        return HealthStatus.CRITICAL;
    }

    private HealthStatus pressure(int sbp) {
        if (sbp <= 0)
            return HealthStatus.GOOD; // No data
        if (sbp >= 110 && sbp <= 130)
            return HealthStatus.GOOD;
        if (sbp >= 100 && sbp <= 140)
            return HealthStatus.AVERAGE;
        if (sbp >= 90 && sbp <= 160)
            return HealthStatus.BAD;
        return HealthStatus.CRITICAL;
    }

    private HealthStatus heartRate(int hr) {
        if (hr <= 0)
            return HealthStatus.GOOD; // No data
        if (hr >= 60 && hr <= 100)
            return HealthStatus.GOOD;
        if (hr >= 50 && hr <= 110)
            return HealthStatus.AVERAGE;
        if (hr >= 40 && hr <= 130)
            return HealthStatus.BAD;
        return HealthStatus.CRITICAL;
    }

    private HealthStatus temperature(double t) {
        if (t <= 0)
            return HealthStatus.GOOD; // No data
        if (t >= 36.1 && t <= 37.5)
            return HealthStatus.GOOD;
        if (t >= 35.5 && t <= 38.0)
            return HealthStatus.AVERAGE;
        if (t >= 35.0 && t <= 39.0)
            return HealthStatus.BAD;
        return HealthStatus.CRITICAL;
    }

    private HealthStatus sugar(double s) {
        if (s <= 0)
            return HealthStatus.GOOD; // No data
        if (s >= 70 && s <= 140)
            return HealthStatus.GOOD;
        if (s >= 60 && s <= 180)
            return HealthStatus.AVERAGE;
        if (s >= 50 && s <= 250)
            return HealthStatus.BAD;
        return HealthStatus.CRITICAL;
    }
}
