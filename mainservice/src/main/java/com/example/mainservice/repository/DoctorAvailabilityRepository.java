
package com.example.mainservice.repository;

import com.example.mainservice.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorIdAndAvailableDateAndIsBookedFalse(
            Long doctorId,
            LocalDate date
    );

    List<DoctorAvailability> findByDoctorId(Long doctorId);
}
