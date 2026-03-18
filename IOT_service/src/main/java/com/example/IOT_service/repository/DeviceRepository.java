package com.example.IOT_service.repository;

import com.example.IOT_service.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceIdAndDeviceTokenAndStatus(String deviceId, String deviceToken, String status);
}