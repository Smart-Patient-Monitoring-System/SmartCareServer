package com.example.mainservice.repository;

import com.example.mainservice.entity.EmergencyContact;
import com.example.mainservice.entity.Hospital;
import com.example.mainservice.entity.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByUserId(Long userId);
    List<EmergencyContact> findByUserIdAndIsPrimary(Long userId, Boolean isPrimary);
    Optional<EmergencyContact> findByUserIdAndId(Long userId, Long id);
}