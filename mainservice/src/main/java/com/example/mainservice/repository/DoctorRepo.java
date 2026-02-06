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

    /*
     * Search doctors by name (case-insensitive, partial match)
     * Example: "john" will match "Dr. John Smith"
     */
    List<Doctor> findByNameContainingIgnoreCase(String name);

    /**
     * Search by doctor registration number (partial match)
     */
    List<Doctor> findByDoctorRegNoContaining(String doctorRegNo);

    /**
     * Search by name OR doctor registration number
     * This provides the best user experience for searching
     */
    List<Doctor> findByNameContainingIgnoreCaseOrDoctorRegNoContaining(
            String name,
            String doctorRegNo
    );

    /**
     * Search by hospital (useful for filtering)
     */
    List<Doctor> findByHospitalContainingIgnoreCase(String hospital);
}
