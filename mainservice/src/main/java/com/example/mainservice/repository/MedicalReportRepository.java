package com.example.mainservice.repository;

import com.example.mainservice.entity.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {
}
