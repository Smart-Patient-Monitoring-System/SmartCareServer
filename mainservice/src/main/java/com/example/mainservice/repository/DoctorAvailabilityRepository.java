package com.example.mainservice.repository;

import com.example.mainservice.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    /** Available (not booked) slots for a SpecialDoctor on a date */
    List<DoctorAvailability> findBySpecialDoctorIdAndAvailableDateAndIsBookedFalse(
            Long doctorId, LocalDate date);

    /** ALL slots for a SpecialDoctor (any status, any date) */
    List<DoctorAvailability> findBySpecialDoctorId(Long doctorId);

    /** Check if a slot already exists to prevent duplicates */
    boolean existsBySpecialDoctorIdAndAvailableDateAndAvailableTime(
            Long doctorId, LocalDate availableDate, LocalTime availableTime);
}
