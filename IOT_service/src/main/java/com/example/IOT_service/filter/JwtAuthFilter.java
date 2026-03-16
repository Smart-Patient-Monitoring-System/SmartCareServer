package com.example.IOT_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    // Points to mainservice — uses container name inside Docker, localhost outside
    @Value("${auth.service.url:http://mainservice:8080}")
    private String authServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip auth for health / actuator endpoints
        if (path.contains("/health") || path.contains("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "Missing Authorization header — attach Bearer token");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Call mainservice /api/auth/validate
            ResponseEntity<Map> authResponse = restTemplate.exchange(
                    authServiceUrl + "/api/auth/validate",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (authResponse.getStatusCode().is2xxSuccessful() && authResponse.getBody() != null) {
                Long userId = Long.valueOf(authResponse.getBody().get("userId").toString());
                // Make userId available to every controller via request attribute
                request.setAttribute("userId", userId);
                chain.doFilter(request, response);
            } else {
                sendUnauthorized(response, "Token validation failed");
            }

        } catch (Exception e) {
            log.error("JWT validation error calling mainservice: {}", e.getMessage());
            sendUnauthorized(response, "Invalid or expired token");
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
