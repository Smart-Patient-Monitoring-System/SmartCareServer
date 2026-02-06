package com.example.mainservice.repository;

import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.PendingDoctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingDoctorRepo extends JpaRepository<PendingDoctor, Long>{
    Optional<PendingDoctor> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
