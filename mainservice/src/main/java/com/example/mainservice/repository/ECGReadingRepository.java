package com.example.mainservice.repository;

import com.example.mainservice.entity.ECGReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ECGReadingRepository extends JpaRepository<ECGReading, Long> {
    List<ECGReading> findByPatientIdOrderByRecordedAtDesc(Long patientId);

    ECGReading findFirstByPatientIdOrderByRecordedAtDesc(Long patientId);

    List<ECGReading> findByPatient_AssignedDoctorIdOrderByRecordedAtDesc(Long doctorId);
}
