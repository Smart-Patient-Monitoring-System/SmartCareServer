package com.example.mainservice.repository;

import com.example.mainservice.entity.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {
    List<EmergencyAlert> findByUserId(Long userId);
    List<EmergencyAlert> findByUserIdAndStatus(Long userId, String status);
    Optional<EmergencyAlert> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}