package com.example.IOT_service.repository;

import com.example.IOT_service.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // Get all data ordered by most recent first
    List<SensorData> findAllByOrderByReceivedAtDesc();

    // Get data within time range
    List<SensorData> findByReceivedAtBetweenOrderByReceivedAtDesc(
            LocalDateTime start,
            LocalDateTime end
    );

    // Get latest N records
    @Query(value = "SELECT * FROM sensor_data ORDER BY received_at DESC LIMIT ?1",
            nativeQuery = true)
    List<SensorData> findLatestN(int limit);

    // Get the most recent reading
    Optional<SensorData> findTopByOrderByReceivedAtDesc();

    // Get data from last N hours
    @Query("SELECT s FROM SensorData s WHERE s.receivedAt >= :since ORDER BY s.receivedAt DESC")
    List<SensorData> findRecentData(LocalDateTime since);
}
