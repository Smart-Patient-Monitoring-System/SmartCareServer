package com.example.IOT_service.controller;

import com.example.IOT_service.model.SensorData;
import com.example.IOT_service.service.SensorDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sensordata")
@RequiredArgsConstructor
@Slf4j
public class SensorDataController {

    private final SensorDataService service;

    // ── ESP32 posts here — no auth needed ─────────────────────────────────────
    // ESP32 includes userId in the JSON body itself
    @PostMapping
    public ResponseEntity<?> receive(@RequestBody SensorData data) {
        SensorData saved = service.save(data);
        log.info("Saved: id={} userId={} bpm={} spo2={}", saved.getId(), saved.getUserId(), saved.getBpm(), saved.getSpo2());
        return ResponseEntity.ok(Map.of("success", true, "id", saved.getId()));
    }

    // ── Frontend fetches latest 50 readings for a patient ─────────────────────
    // GET /api/sensordata/patient/1/latest/50
    @GetMapping("/patient/{userId}/latest/{limit}")
    public ResponseEntity<List<SensorData>> getLatest(@PathVariable Long userId,
                                                       @PathVariable int limit) {
        return ResponseEntity.ok(service.getLatestForUser(userId, limit));
    }

    // ── Frontend fetches single current reading ────────────────────────────────
    // GET /api/sensordata/patient/1/current
    @GetMapping("/patient/{userId}/current")
    public ResponseEntity<?> getCurrent(@PathVariable Long userId) {
        return service.getLatestOne(userId)
                .map(d -> ResponseEntity.ok((Object) d))
                .orElse(ResponseEntity.ok(Map.of("message", "No data yet")));
    }

    // ── Health check ──────────────────────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "healthy", "timestamp", LocalDateTime.now()));
    }
}
