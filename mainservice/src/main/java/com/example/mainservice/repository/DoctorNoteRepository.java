package com.example.mainservice.repository;

import com.example.mainservice.entity.DoctorNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorNoteRepository extends JpaRepository<DoctorNote, Long> {
    List<DoctorNote> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
