package com.example.IOT_service.service;

import com.example.IOT_service.model.Device;
import com.example.IOT_service.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public Device validateDevice(String deviceId, String deviceToken) {
        return deviceRepository
                .findByDeviceIdAndDeviceTokenAndStatus(deviceId, deviceToken, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Invalid or inactive device"));
    }

    public Device assignDeviceToUser(String deviceId, Long userId) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        device.setUserId(userId);
        device.setStatus("ACTIVE");
        return deviceRepository.save(device);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }
}