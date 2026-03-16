package com.example.IOT_service.repository;

import com.example.IOT_service.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // All data for a specific user, newest first
    List<SensorData> findByUserIdOrderByReceivedAtDesc(Long userId);

    // Most recent single reading for a user
    Optional<SensorData> findTopByUserIdOrderByReceivedAtDesc(Long userId);

    // Latest N records for a user
    @Query(value = "SELECT * FROM sensor_data WHERE user_id = ?1 ORDER BY received_at DESC LIMIT ?2",
            nativeQuery = true)
    List<SensorData> findLatestNForUser(Long userId, int limit);

    // Records newer than `since` for a user
    @Query("SELECT s FROM SensorData s WHERE s.userId = :userId AND s.receivedAt >= :since ORDER BY s.receivedAt DESC")
    List<SensorData> findRecentDataForUser(Long userId, LocalDateTime since);

    // Cleanup
    @Modifying
    @Query("DELETE FROM SensorData s WHERE s.receivedAt < :cutoff")
    void deleteByReceivedAtBefore(LocalDateTime cutoff);
}
