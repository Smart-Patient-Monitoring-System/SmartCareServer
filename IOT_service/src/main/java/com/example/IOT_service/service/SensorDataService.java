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

    // Get latest N readings for a user, falls back to latest overall if none found
    public List<SensorData> getLatestForUser(Long userId, int limit) {
        List<SensorData> data = repository.findLatestForUser(userId, limit);
        if (!data.isEmpty()) return data;
        // Fallback: return latest readings regardless of user
        return repository.findAll()
                .stream()
                .sorted((a, b) -> b.getReceivedAt().compareTo(a.getReceivedAt()))
                .limit(limit)
                .toList();
    }

    // Single most recent reading for a user — used for "current" display
    // Falls back to latest overall reading if nothing found for this user
    public Optional<SensorData> getLatestOne(Long userId) {
        Optional<SensorData> userReading = repository.findTopByUserIdOrderByReceivedAtDesc(userId);
        if (userReading.isPresent()) return userReading;
        return repository.findTopByOrderByReceivedAtDesc(); // DB fallback
    }
}
