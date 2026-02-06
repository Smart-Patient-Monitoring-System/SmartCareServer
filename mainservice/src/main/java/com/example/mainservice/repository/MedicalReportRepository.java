package com.example.mainservice.repository;

import com.example.mainservice.entity.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {

    List<MedicalReport> findByPatient_IdOrderByUploadedAtDesc(Long patientId);
}
