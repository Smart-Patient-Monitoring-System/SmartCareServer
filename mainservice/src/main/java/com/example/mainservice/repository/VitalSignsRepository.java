// File: src/main/java/com/example/mainservice/repository/VitalSignsRepository.java

package com.example.mainservice.repository;

import com.example.mainservice.entity.VitalSigns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {

    // Find all vital signs for a specific patient, ordered by measurement date descending
    List<VitalSigns> findByPatientIdOrderByMeasurementDateTimeDesc(Long patientId);

    // Find vital signs within a date range
    @Query("SELECT v FROM VitalSigns v WHERE v.patientId = :patientId " +
            "AND v.measurementDateTime BETWEEN :startDate AND :endDate " +
            "ORDER BY v.measurementDateTime DESC")
    List<VitalSigns> findByPatientIdAndDateRange(
            @Param("patientId") Long patientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Find latest vital signs for a patient
    VitalSigns findFirstByPatientIdOrderByMeasurementDateTimeDesc(Long patientId);
}