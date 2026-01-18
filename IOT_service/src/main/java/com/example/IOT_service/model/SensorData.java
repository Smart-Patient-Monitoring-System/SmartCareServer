package com.example.IOT_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_temp")
    private Double roomTemp;

    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "water_temp_c")
    private Double waterTempC;

    @Column(name = "water_temp_f")
    private Double waterTempF;

    @Column(name = "ir_value")
    private Long irValue;

    @Column(name = "bpm")
    private Double bpm;

    @Column(name = "avg_bpm")
    private Integer avgBpm;

    @Column(name = "spo2")
    private Integer spo2;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}
