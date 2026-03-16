package com.example.IOT_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device_registry")
@Data
@NoArgsConstructor
public class DeviceRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Must match the hardcoded DEVICE_ID on the ESP32
    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId;

    // References user.id from mainservice (cross-service — store as plain Long)
    @Column(name = "user_id", nullable = false)
    private Long userId;
}
