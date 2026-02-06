package com.example.mainservice.repository;

import com.example.mainservice.entity.LoginOtpSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface LoginOtpSessionRepository extends JpaRepository<LoginOtpSession, Long> {
    Optional<LoginOtpSession> findBySessionId(String sessionId);

    @Transactional
    void deleteByPatientId(Long patientId);

    @Transactional
    void deleteByPatientIdAndRole(Long patientId, String role);
}

