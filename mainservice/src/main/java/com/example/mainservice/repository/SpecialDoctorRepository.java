package com.example.mainservice.repository;

import com.example.mainservice.entity.SpecialDoctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecialDoctorRepository extends JpaRepository<SpecialDoctor, Long> {
    Optional<SpecialDoctor> findByEmail(String email);
}
