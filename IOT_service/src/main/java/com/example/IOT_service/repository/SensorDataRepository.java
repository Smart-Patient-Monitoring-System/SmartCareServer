package com.example.IOT_service.repository;

import com.example.IOT_service.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // Latest N records for a specific patient
    @Query(value = "SELECT * FROM sensor_data WHERE user_id = ?1 ORDER BY received_at DESC LIMIT ?2",
           nativeQuery = true)
    List<SensorData> findLatestForUser(Long userId, int limit);

    // Most recent single record for a patient (used as fallback)
    Optional<SensorData> findTopByUserIdOrderByReceivedAtDesc(Long userId);

    // Most recent record across ALL patients (for ESP32 that posts without userId)
    Optional<SensorData> findTopByOrderByReceivedAtDesc();
}
