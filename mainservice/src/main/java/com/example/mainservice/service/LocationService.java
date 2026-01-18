package com.example.mainservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LocationService {

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LocationService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get coordinates from address using Google Geocoding API
     * Returns array: [latitude, longitude]
     */
    public Double[] getCoordinatesFromAddress(String address) {
        try {
            // If no API key, use default coordinates for Colombo
            if (googleMapsApiKey == null || googleMapsApiKey.isEmpty()) {
                System.out.println("No Google Maps API key found. Using default coordinates.");
                return getDefaultCoordinates();
            }

            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                    address.replace(" ", "+"),
                    googleMapsApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.has("results") && root.get("results").size() > 0) {
                JsonNode location = root.get("results").get(0)
                        .get("geometry").get("location");

                Double lat = location.get("lat").asDouble();
                Double lng = location.get("lng").asDouble();

                return new Double[]{lat, lng};
            }

            return getDefaultCoordinates();

        } catch (Exception e) {
            System.err.println("Geocoding error: " + e.getMessage());
            return getDefaultCoordinates();
        }
    }

    /**
     * Get address from coordinates (Reverse Geocoding)
     */
    public String getAddressFromCoordinates(Double latitude, Double longitude) {
        try {
            if (googleMapsApiKey == null || googleMapsApiKey.isEmpty()) {
                return String.format("Lat: %.4f, Lng: %.4f", latitude, longitude);
            }

            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
                    latitude, longitude, googleMapsApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.has("results") && root.get("results").size() > 0) {
                return root.get("results").get(0)
                        .get("formatted_address").asText();
            }

            return String.format("Lat: %.4f, Lng: %.4f", latitude, longitude);

        } catch (Exception e) {
            System.err.println("Reverse geocoding error: " + e.getMessage());
            return String.format("Lat: %.4f, Lng: %.4f", latitude, longitude);
        }
    }

    /**
     * Default coordinates for Colombo, Sri Lanka
     */
    private Double[] getDefaultCoordinates() {
        return new Double[]{6.9271, 79.8612}; // Colombo city center
    }
}