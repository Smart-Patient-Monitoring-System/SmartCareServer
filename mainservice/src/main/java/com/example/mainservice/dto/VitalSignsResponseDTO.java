// File: src/main/java/com/example/mainservice/dto/VitalSignsResponseDTO.java

package com.example.mainservice.dto;

import java.time.LocalDateTime;

public class VitalSignsResponseDTO {

    private Long id;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private Double bloodSugar;
    private Double temperature;
    private Integer heartRate;
    private Integer spo2;
    private Double weight;
    private LocalDateTime measurementDateTime;
    private String notes;
    private LocalDateTime createdAt;

    // Constructor
    public VitalSignsResponseDTO() {}

    public VitalSignsResponseDTO(Long id, Integer bloodPressureSystolic,
                                 Integer bloodPressureDiastolic, Double bloodSugar,
                                 Double temperature, Integer heartRate, Integer spo2,
                                 Double weight, LocalDateTime measurementDateTime,
                                 String notes, LocalDateTime createdAt) {
        this.id = id;
        this.bloodPressureSystolic = bloodPressureSystolic;
        this.bloodPressureDiastolic = bloodPressureDiastolic;
        this.bloodSugar = bloodSugar;
        this.temperature = temperature;
        this.heartRate = heartRate;
        this.spo2 = spo2;
        this.weight = weight;
        this.measurementDateTime = measurementDateTime;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getBloodPressureSystolic() {
        return bloodPressureSystolic;
    }

    public void setBloodPressureSystolic(Integer bloodPressureSystolic) {
        this.bloodPressureSystolic = bloodPressureSystolic;
    }

    public Integer getBloodPressureDiastolic() {
        return bloodPressureDiastolic;
    }

    public void setBloodPressureDiastolic(Integer bloodPressureDiastolic) {
        this.bloodPressureDiastolic = bloodPressureDiastolic;
    }

    public Double getBloodSugar() {
        return bloodSugar;
    }

    public void setBloodSugar(Double bloodSugar) {
        this.bloodSugar = bloodSugar;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Integer getSpo2() {
        return spo2;
    }

    public void setSpo2(Integer spo2) {
        this.spo2 = spo2;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public LocalDateTime getMeasurementDateTime() {
        return measurementDateTime;
    }

    public void setMeasurementDateTime(LocalDateTime measurementDateTime) {
        this.measurementDateTime = measurementDateTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}