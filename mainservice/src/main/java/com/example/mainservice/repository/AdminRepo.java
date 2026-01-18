package com.example.mainservice.repository;

import com.example.mainservice.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepo extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
