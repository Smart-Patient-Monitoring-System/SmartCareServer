package com.example.IOT_service.repository;

import com.example.IOT_service.model.DeviceRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DeviceRegistryRepository extends JpaRepository<DeviceRegistry, Long> {
    Optional<DeviceRegistry> findByDeviceId(String deviceId);
}
