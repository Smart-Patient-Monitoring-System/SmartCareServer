package com.example.vitalReports.logic;

import com.example.vitalReports.domain.enums.HealthStatus;
import com.example.vitalReports.domain.model.VitalReading;
import com.example.vitalReports.domain.model.VitalStatus;
import org.springframework.stereotype.Component;

@Component
public class AIVitalEvaluator implements VitalDecisionEngine {

    @Override
    public VitalStatus evaluate(VitalReading reading) {

        // 🔮 Placeholder for real ML model
        // Later: REST call / ONNX / TensorFlow / Python

        return new VitalStatus(
                HealthStatus.AVERAGE,
                HealthStatus.AVERAGE,
                HealthStatus.AVERAGE,
                HealthStatus.AVERAGE
        );
    }
}
