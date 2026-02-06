package com.example.IOT_service.controller;

import com.example.IOT_service.model.SensorData;
import com.example.IOT_service.service.SensorDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sensordata")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class SensorDataController {

    private final SensorDataService service;

    // POST endpoint - receives data from ESP32
    @PostMapping
    public ResponseEntity<?> receiveSensorData(@RequestBody SensorData data) {
        try {
            log.info("Received sensor data: RoomTemp={}, Humidity={}, WaterTemp={}, BPM={}, AvgBPM={}, SpO2={}",
                    data.getRoomTemp(), data.getHumidity(), data.getWaterTempC(),
                    data.getBpm(), data.getAvgBpm(), data.getSpo2());

            SensorData saved = service.saveSensorData(data);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Data saved successfully",
                    "id", saved.getId(),
                    "timestamp", saved.getReceivedAt()
            ));

        } catch (Exception e) {
            log.error("Error saving sensor data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error: " + e.getMessage()
                    ));
        }
    }

    // GET all data
    @GetMapping
    public ResponseEntity<List<SensorData>> getAllData() {
        return ResponseEntity.ok(service.getAllData());
    }

    // GET latest N records
    @GetMapping("/latest/{limit}")
    public ResponseEntity<List<SensorData>> getLatestData(@PathVariable int limit) {
        return ResponseEntity.ok(service.getLatestData(limit));
    }

    // GET most recent single reading
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentReading() {
        return service.getLatestReading()
                .map(data -> ResponseEntity.ok((Object) data))
                .orElse(ResponseEntity.ok(Map.of("message", "No data available")));
    }

    // GET data from last N hours
    @GetMapping("/recent/{hours}")
    public ResponseEntity<List<SensorData>> getRecentData(@PathVariable int hours) {
        return ResponseEntity.ok(service.getRecentData(hours));
    }

    // DELETE old data
    @DeleteMapping("/cleanup/{days}")
    public ResponseEntity<?> cleanupOldData(@PathVariable int days) {
        try {
            service.deleteOldData(days);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Deleted data older than " + days + " days"
            ));
        } catch (Exception e) {
            log.error("Error cleaning up data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Health check
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "timestamp", LocalDateTime.now(),
                "service", "Sensor Data API"
        ));
    }

    @GetMapping("/latest/{limit}/with-status")
    public ResponseEntity<?> getLatestWithStatus(@PathVariable int limit) {
        List<SensorData> data = service.getLatestData(limit);
        if (data.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "data", List.of(),
                    "stale", true,
                    "ageSeconds", null,
                    "latestTimestamp", null
            ));
        }

        SensorData latest = data.get(0); // because repository returns DESC
        long ageSeconds = java.time.Duration.between(latest.getReceivedAt(), LocalDateTime.now()).getSeconds();
        boolean stale = ageSeconds > 30; // choose threshold

        return ResponseEntity.ok(Map.of(
                "data", data,
                "latestTimestamp", latest.getReceivedAt(),
                "ageSeconds", ageSeconds,
                "stale", stale
        ));
    }

}
