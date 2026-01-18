package com.example.vitalReports.domain.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class VitalReading {

    // SpO2 percentage (0–100)
    @Min(value = 0, message = "SpO2 cannot be less than 0")
    @Max(value = 100, message = "SpO2 cannot be greater than 100")
    private int spo2;

    // Systolic Blood Pressure (mmHg)
    @Min(value = 50, message = "Systolic BP is too low")
    @Max(value = 250, message = "Systolic BP is too high")
    private int systolicBP;

    // Heart Rate (beats per minute)
    @Min(value = 30, message = "Heart rate is too low")
    @Max(value = 220, message = "Heart rate is too high")
    private int heartRate;

    // Body temperature in Celsius
    @Min(value = 30, message = "Temperature is too low")
    @Max(value = 45, message = "Temperature is too high")
    private double temperature;

    // Unix timestamp from IoT device
    @NotNull(message = "Timestamp is required")
    private Long timestamp;

    // 🔹 Required by Spring (JSON → Object)
    public VitalReading() {
    }

    // 🔹 Optional constructor
    public VitalReading(int spo2, int systolicBP, int heartRate,
                        double temperature, Long timestamp) {
        this.spo2 = spo2;
        this.systolicBP = systolicBP;
        this.heartRate = heartRate;
        this.temperature = temperature;
        this.timestamp = timestamp;
    }

    public int getSpo2() {
        return spo2;
    }

    public int getSystolicBP() {
        return systolicBP;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public double getTemperature() {
        return temperature;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
