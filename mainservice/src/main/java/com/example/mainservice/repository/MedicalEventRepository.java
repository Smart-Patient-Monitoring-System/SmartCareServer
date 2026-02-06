package com.example.mainservice.repository;

import com.example.mainservice.entity.MedicalEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicalEventRepository extends JpaRepository<MedicalEvent, Long> {

    List<MedicalEvent> findByPatient_IdOrderByRecordedAtDesc(Long patientId);

    @Query("""
        select e from MedicalEvent e
        where e.patient.id = :patientId
          and (:from is null or e.recordedAt >= :from)
          and (:to is null or e.recordedAt <= :to)
        order by e.recordedAt desc
    """)
    List<MedicalEvent> findInRange(
            @Param("patientId") Long patientId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        select e from MedicalEvent e
        where e.patient.id = :patientId and e.type = :type
        order by e.recordedAt desc
    """)
    List<MedicalEvent> findByPatientAndType(@Param("patientId") Long patientId, @Param("type") String type);
}
