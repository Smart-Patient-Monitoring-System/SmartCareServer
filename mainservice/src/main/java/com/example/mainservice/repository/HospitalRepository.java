package com.example.mainservice.repository;

import com.example.mainservice.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    @Query(value = "SELECT *, " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(latitude)))) AS distance " +
            "FROM hospitals " +
            "WHERE has_emergency_ward = true " +
            "ORDER BY distance " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Hospital> findNearestHospitals(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("limit") Integer limit
    );

    List<Hospital> findByHasEmergencyWard(Boolean hasEmergencyWard);
}