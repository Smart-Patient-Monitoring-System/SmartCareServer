package com.example.mainservice.repository;

import com.example.mainservice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepo extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUsername(String username);

    Optional<Doctor> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Doctor> findByNameContainingIgnoreCase(String name);

    List<Doctor> findByDoctorRegNoContaining(String doctorRegNo);

    List<Doctor> findByNameContainingIgnoreCaseOrDoctorRegNoContaining(String name, String doctorRegNo);

    List<Doctor> findByHospitalContainingIgnoreCase(String hospital);

    // Doctor assignment: find by position for round-robin assignment
    List<Doctor> findByPosition(String position);

    List<Doctor> findByPositionContainingIgnoreCase(String position);
}
