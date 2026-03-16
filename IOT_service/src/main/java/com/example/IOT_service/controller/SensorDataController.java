package com.example.IOT_service.controller;

import com.example.IOT_service.model.SensorData;
import com.example.IOT_service.service.SensorDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sensordata")
@RequiredArgsConstructor
@Slf4j
public class SensorDataController {

    private final SensorDataService service;

    // SSE emitters keyed by userId — enables real-time push to frontend
    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // ── POST: ESP32 sends data ────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> receiveSensorData(@RequestBody SensorData data,
                                               HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            data.setUserId(userId);

            SensorData saved = service.saveSensorData(data);
            log.info("Saved sensor data id={} userId={} deviceId={}", saved.getId(), userId, saved.getDeviceId());

            // Push to any connected SSE client for this user in real-time
            pushToSseClient(userId, saved);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", saved.getId(),
                    "timestamp", saved.getReceivedAt()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error saving sensor data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ── SSE: frontend subscribes here for real-time data ─────────────────────
    // Usage: GET /api/sensordata/stream   (with Authorization header)
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSensorData(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        // 5-minute timeout; frontend should reconnect automatically
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            emitter.complete();
        });
        emitter.onError(e -> emitters.remove(userId));

        // Send current latest reading immediately on connect
        service.getLatestReadingForUser(userId).ifPresent(data -> {
            try {
                emitter.send(SseEmitter.event().name("sensorData").data(data));
            } catch (IOException ignored) {}
        });

        // Heartbeat every 30s to keep the connection alive through proxies
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (emitters.containsKey(userId)) {
                    emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
                }
            } catch (Exception e) {
                emitters.remove(userId);
            }
        }, 30, 30, TimeUnit.SECONDS);

        return emitter;
    }

    // ── GET: authenticated user's own data only ───────────────────────────────
    @GetMapping
    public ResponseEntity<List<SensorData>> getMyData(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(service.getAllDataForUser(userId));
    }

    @GetMapping("/latest/{limit}")
    public ResponseEntity<List<SensorData>> getLatestData(@PathVariable int limit,
                                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(service.getLatestDataForUser(userId, limit));
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentReading(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return service.getLatestReadingForUser(userId)
                .map(data -> ResponseEntity.ok((Object) data))
                .orElse(ResponseEntity.ok(Map.of("message", "No data available yet")));
    }

    @GetMapping("/recent/{hours}")
    public ResponseEntity<List<SensorData>> getRecentData(@PathVariable int hours,
                                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(service.getRecentDataForUser(userId, hours));
    }

    @GetMapping("/latest/{limit}/with-status")
    public ResponseEntity<?> getLatestWithStatus(@PathVariable int limit,
                                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<SensorData> data = service.getLatestDataForUser(userId, limit);
        if (data.isEmpty()) {
            return ResponseEntity.ok(Map.of("data", List.of(), "stale", true,
                    "ageSeconds", null, "latestTimestamp", null));
        }
        SensorData latest = data.get(0);
        long ageSeconds = java.time.Duration.between(latest.getReceivedAt(), LocalDateTime.now()).getSeconds();
        return ResponseEntity.ok(Map.of(
                "data", data,
                "latestTimestamp", latest.getReceivedAt(),
                "ageSeconds", ageSeconds,
                "stale", ageSeconds > 30
        ));
    }

    @DeleteMapping("/cleanup/{days}")
    public ResponseEntity<?> cleanupOldData(@PathVariable int days) {
        try {
            service.deleteOldData(days);
            return ResponseEntity.ok(Map.of("success", true,
                    "message", "Deleted data older than " + days + " days"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ── Health check (public, no auth needed) ────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "timestamp", LocalDateTime.now(),
                "service", "IoT Sensor Data API",
                "activeStreams", emitters.size()
        ));
    }

    // ── Internal helper: push new reading to the user's SSE stream ───────────
    private void pushToSseClient(Long userId, SensorData data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event().name("sensorData").data(data));
        } catch (Exception e) {
            emitters.remove(userId);
        }
    }
}
