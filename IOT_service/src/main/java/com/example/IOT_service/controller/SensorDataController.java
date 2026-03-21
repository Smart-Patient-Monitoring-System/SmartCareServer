package com.example.IOT_service.controller;

import com.example.IOT_service.model.Device;
import com.example.IOT_service.model.SensorData;
import com.example.IOT_service.service.DeviceService;
import com.example.IOT_service.service.SensorDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.IOT_service.dto.DeviceAssignmentRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sensordata")
@RequiredArgsConstructor
@Slf4j
public class SensorDataController {

    private final SensorDataService sensorDataService;
    private final DeviceService deviceService;

    // Device upload endpoint for hosted use
    @PostMapping("/device/upload")
    public ResponseEntity<?> receiveFromDevice(
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-Device-Token") String deviceToken,
            @RequestBody SensorData data) {

        log.info("Incoming device upload: deviceId={} bpm={} spo2={}", deviceId, data.getBpm(), data.getSpo2());

        Device device = deviceService.validateDevice(deviceId, deviceToken);

        data.setUserId(device.getUserId());
        data.setDeviceId(device.getDeviceId());

        SensorData saved = sensorDataService.save(data);

        log.info("Saved: id={} userId={} deviceId={} bpm={} spo2={}",
                saved.getId(),
                saved.getUserId(),
                saved.getDeviceId(),
                saved.getBpm(),
                saved.getSpo2());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "id", saved.getId(),
                "userId", saved.getUserId(),
                "deviceId", saved.getDeviceId()
        ));
    }

    // Optional localhost test endpoint - keep only during testing
    @PostMapping
    public ResponseEntity<?> receiveLocalTest(@RequestBody SensorData data) {
        SensorData saved = sensorDataService.save(data);
        return ResponseEntity.ok(Map.of("success", true, "id", saved.getId()));
    }

    @GetMapping("/patient/{userId}/latest/{limit}")
    public ResponseEntity<List<SensorData>> getLatest(@PathVariable Long userId,
                                                      @PathVariable int limit) {
        return ResponseEntity.ok(sensorDataService.getLatestForUser(userId, limit));
    }

    @GetMapping("/patient/{userId}/latest")
    public ResponseEntity<List<SensorData>> getLatestDefault(@PathVariable Long userId) {
        return ResponseEntity.ok(sensorDataService.getLatestForUser(userId, 1));
    }

    @GetMapping("/patient/{userId}/current")
    public ResponseEntity<?> getCurrent(@PathVariable Long userId) {
        return sensorDataService.getLatestOne(userId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of("message", "No data yet")));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "timestamp", LocalDateTime.now()
        ));
    }

    @PostMapping("/devices/assign")
    public ResponseEntity<?> assignDevice(@RequestBody DeviceAssignmentRequest request) {
        Device device = deviceService.assignDeviceToUser(request.getDeviceId(), request.getUserId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Device assigned successfully",
                "deviceId", device.getDeviceId(),
                "userId", device.getUserId()
        ));
    }

    @GetMapping("/devices")
    public ResponseEntity<?> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }
}