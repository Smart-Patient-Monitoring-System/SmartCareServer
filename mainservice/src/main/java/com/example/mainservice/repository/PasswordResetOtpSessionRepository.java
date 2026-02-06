package com.example.mainservice.repository;

import com.example.mainservice.entity.PasswordResetOtpSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetOtpSessionRepository extends JpaRepository<PasswordResetOtpSession, Long> {
    Optional<PasswordResetOtpSession> findBySessionId(String sessionId);

    @Transactional
    void deleteByUsernameAndRole(String username, String role);
}

