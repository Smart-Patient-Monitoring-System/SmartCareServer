package com.example.vitalReports.domain.model;

import com.example.vitalReports.domain.enums.HealthStatus;

public class VitalStatus {

    private HealthStatus spo2Status;
    private HealthStatus pressureStatus;
    private HealthStatus heartRateStatus;
    private HealthStatus temperatureStatus;

    public VitalStatus(HealthStatus spo2Status,
                       HealthStatus pressureStatus,
                       HealthStatus heartRateStatus,
                       HealthStatus temperatureStatus) {
        this.spo2Status = spo2Status;
        this.pressureStatus = pressureStatus;
        this.heartRateStatus = heartRateStatus;
        this.temperatureStatus = temperatureStatus;
    }

    public HealthStatus getSpo2Status() {
        return spo2Status;
    }

    public HealthStatus getPressureStatus() {
        return pressureStatus;
    }

    public HealthStatus getHeartRateStatus() {
        return heartRateStatus;
    }

    public HealthStatus getTemperatureStatus() {
        return temperatureStatus;
    }
}
