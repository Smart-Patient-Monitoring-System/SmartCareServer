package com.example.mainservice.repository;

import com.example.mainservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepo extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUsername(String username);

    Optional<Patient> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // For doctor-side patient search in chat
    List<Patient> findByNameContainingIgnoreCaseOrNicNoContainingIgnoreCase(String name, String nicNo);
}
