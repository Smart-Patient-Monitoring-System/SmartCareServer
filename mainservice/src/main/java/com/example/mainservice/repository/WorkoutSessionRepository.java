package com.example.mainservice.repository;

import com.example.mainservice.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    List<WorkoutSession> findByPatientIdOrderByUploadDateDesc(Long patientId);

    List<WorkoutSession> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    void deleteByIdAndPatientId(Long id, Long patientId);

    boolean existsByIdAndPatientId(Long id, Long patientId);

    boolean existsByPatientIdAndContentHash(Long patientId, String contentHash);
}
