package com.example.IOT_service.service;

import com.example.IOT_service.model.DeviceRegistry;
import com.example.IOT_service.model.SensorData;
import com.example.IOT_service.repository.DeviceRegistryRepository;
import com.example.IOT_service.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SensorDataService {

    private final SensorDataRepository repository;
    private final DeviceRegistryRepository deviceRegistryRepository;

    @Transactional
    public SensorData saveSensorData(SensorData data) {
        // If ESP32 sent a deviceId, verify it is registered to the authenticated user
        if (data.getDeviceId() != null && !data.getDeviceId().isBlank()) {
            Optional<DeviceRegistry> device = deviceRegistryRepository
                    .findByDeviceId(data.getDeviceId());

            if (device.isPresent() && !device.get().getUserId().equals(data.getUserId())) {
                throw new IllegalArgumentException(
                        "Device '" + data.getDeviceId() + "' is not registered to this account");
            }
            // If device not found in registry we allow it (unregistered device falls back
            // to JWT-based userId only). You can change this to throw if you want strict mode.
        }
        return repository.save(data);
    }

    public List<SensorData> getAllDataForUser(Long userId) {
        return repository.findByUserIdOrderByReceivedAtDesc(userId);
    }

    public List<SensorData> getLatestDataForUser(Long userId, int limit) {
        return repository.findLatestNForUser(userId, limit);
    }

    public Optional<SensorData> getLatestReadingForUser(Long userId) {
        return repository.findTopByUserIdOrderByReceivedAtDesc(userId);
    }

    public List<SensorData> getRecentDataForUser(Long userId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return repository.findRecentDataForUser(userId, since);
    }

    @Transactional
    public void deleteOldData(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        repository.deleteByReceivedAtBefore(cutoff);
    }
}
