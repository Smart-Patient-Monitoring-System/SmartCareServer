package com.example.mainservice.repository;

import com.example.mainservice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepo extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
