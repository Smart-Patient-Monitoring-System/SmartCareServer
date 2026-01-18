package com.example.IOT_service.service;

import com.example.IOT_service.model.SensorData;
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

    @Transactional
    public SensorData saveSensorData(SensorData data) {
        return repository.save(data);
    }

    public List<SensorData> getAllData() {
        return repository.findAllByOrderByReceivedAtDesc();
    }

    public List<SensorData> getLatestData(int limit) {
        return repository.findLatestN(limit);
    }

    public Optional<SensorData> getLatestReading() {
        return repository.findTopByOrderByReceivedAtDesc();
    }

    public List<SensorData> getDataInRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByReceivedAtBetweenOrderByReceivedAtDesc(start, end);
    }

    public List<SensorData> getRecentData(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return repository.findRecentData(since);
    }

    @Transactional
    public void deleteOldData(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        List<SensorData> oldData = repository.findByReceivedAtBetweenOrderByReceivedAtDesc(
                LocalDateTime.MIN, cutoff
        );
        repository.deleteAll(oldData);
    }
}
