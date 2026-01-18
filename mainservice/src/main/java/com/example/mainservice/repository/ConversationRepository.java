package com.example.mainservice.repository;

import com.example.mainservice.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find all conversations for a patient
     */
    List<Conversation> findByPatientIdOrderByTimestampDesc(Integer patientId);

    /**
     * Find all conversations for a doctor
     */
    List<Conversation> findByDoctorIdOrderByTimestampDesc(Integer doctorId);

    /**
     * Find conversation between a specific patient and doctor
     */
    Optional<Conversation> findByPatientIdAndDoctorId(Integer patientId, Integer doctorId);

    /**
     * Find all conversations for a user (either as patient or doctor)
     */
    @Query("SELECT c FROM Conversation c WHERE c.patientId = :userId OR c.doctorId = :userId ORDER BY c.timestamp DESC")
    List<Conversation> findAllByUserId(@Param("userId") Integer userId);

    /**
     * Check if conversation exists between patient and doctor
     */
    boolean existsByPatientIdAndDoctorId(Integer patientId, Integer doctorId);
}