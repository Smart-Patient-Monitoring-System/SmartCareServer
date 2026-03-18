package com.example.IOT_service.service;

import com.example.IOT_service.model.SensorData;
import com.example.IOT_service.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SensorDataService {

    private final SensorDataRepository repository;

    public SensorData save(SensorData data) {
        return repository.save(data);
    }

    // Only return this patient's data
    public List<SensorData> getLatestForUser(Long userId, int limit) {
        return repository.findLatestForUser(userId, limit);
    }

    // Return this patient's latest saved reading only
    public Optional<SensorData> getLatestOne(Long userId) {
        return repository.findTopByUserIdOrderByReceivedAtDesc(userId);
    }
}