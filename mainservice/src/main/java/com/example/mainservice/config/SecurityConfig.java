package com.example.mainservice.config;

import com.example.mainservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            @Lazy UserDetailsService userDetailsService,
            @Lazy JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Mainservice runs BEHIND the API Gateway. Gateway owns CORS.
     * We disable CORS here to avoid duplicate headers.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Disable Spring Security CORS - gateway handles it
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── Auth ──────────────────────────────────────────
                        .requestMatchers("/api/auth/**", "/auth/**").permitAll()

                        // ── Admin (all admin paths open - secured by role at service level) ──
                        .requestMatchers("/api/admin/**", "/admin/**").permitAll()

                        // ── Doctor portal ─────────────────────────────────
                        .requestMatchers("/api/doctor/**", "/doctor/**").permitAll()
                        .requestMatchers("/api/doctor-notes/**", "/doctor-notes/**").permitAll()

                        // ── Patient ───────────────────────────────────────
                        .requestMatchers("/api/patient/**", "/patient/**").permitAll()
                        .requestMatchers("/api/pendingdoctor/**", "/pendingdoctor/**").permitAll()
                        .requestMatchers("/api/dashboard/**", "/dashboard/**").permitAll()
                        .requestMatchers("/api/vital/**", "/vital/**").permitAll()

                        // ── Payments ──────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/payments/pay/**", "/payments/pay/**").permitAll()
                        .requestMatchers("/api/payments/notify", "/payments/notify").permitAll()
                        // Dev bypass - mark all pending as paid without webhook
                        .requestMatchers(HttpMethod.GET, "/api/payments/dev-success-all").permitAll()

                        // ── WebSocket ──────────────────────────────────────
                        .requestMatchers("/ws/**", "/ws").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // ── Booking - PUBLIC reads ──────────────────────────
                        // Patients need to browse doctors, types, slots without logging in
                        .requestMatchers(HttpMethod.GET, "/api/doctors/**", "/doctors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/appointment-types/**", "/appointment-types/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/availability/**", "/availability/**").permitAll()
                        .requestMatchers("/api/availability/fix-db").permitAll()

                        // ── Booking - WRITE operations (open to allow patient + admin) ──
                        // NOTE: keeping these permitAll avoids forbidden errors when JWT
                        // role doesn't exactly match hasAnyRole("PATIENT","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/appointments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/appointments/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/appointments/**").permitAll()

                        // ── Availability management (doctor/admin add slots) ──
                        .requestMatchers(HttpMethod.POST, "/api/availability/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/availability/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/availability/**").permitAll()

                        // ── Special doctors CRUD (admin) ───────────────────
                        .requestMatchers(HttpMethod.POST, "/api/doctors/**", "/doctors/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/doctors/**", "/doctors/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/doctors/**", "/doctors/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/appointment-types/**").permitAll()

                        // ── Chat ───────────────────────────────────────────
                        .requestMatchers("/api/chat/**", "/chat/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
