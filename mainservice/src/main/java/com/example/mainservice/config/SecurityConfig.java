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
     * Mainservice runs BEHIND the API Gateway. The gateway owns CORS.
     * Permissive config here prevents duplicate CORS headers.
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── Auth ──────────────────────────────────────────────────
                        .requestMatchers("/api/auth/**", "/auth/**").permitAll()

                        // ── Admin ─────────────────────────────────────────────────
                        .requestMatchers("/api/admin/**", "/admin/**").permitAll()

                        // ── Doctor portal (general doctors) ───────────────────────
                        .requestMatchers("/api/doctor/**", "/doctor/**").permitAll()
                        .requestMatchers("/api/doctor-notes/**", "/doctor-notes/**").permitAll()

                        // ── Patient ───────────────────────────────────────────────
                        .requestMatchers("/api/patient/**", "/patient/**").permitAll()

                        // ── Pending doctors ───────────────────────────────────────
                        .requestMatchers("/api/pendingdoctor/**", "/pendingdoctor/**").permitAll()

                        // ── Dashboard ─────────────────────────────────────────────
                        .requestMatchers("/api/dashboard/**", "/dashboard/**").permitAll()

                        // ── Vitals / ECG ──────────────────────────────────────────
                        .requestMatchers("/api/vital/**", "/vital/**").permitAll()

                        // ── Payments ──────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/payments/pay/**", "/payments/pay/**").permitAll()
                        .requestMatchers("/api/payments/notify", "/payments/notify").permitAll()

                        // ── WebSocket ─────────────────────────────────────────────
                        .requestMatchers("/ws/**", "/ws").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // ── SpecialDoctors (booking doctors) ──────────────────────
                        // GET - public (patient browsing doctors)
                        .requestMatchers(HttpMethod.GET, "/api/doctors/**", "/doctors/**").permitAll()
                        // CRUD - admin only
                        .requestMatchers(HttpMethod.POST, "/api/doctors/**", "/doctors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/doctors/**", "/doctors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/doctors/**", "/doctors/**").hasRole("ADMIN")

                        // ── Appointment types ─────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/appointment-types/**", "/appointment-types/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/appointment-types/**", "/appointment-types/**").hasRole("ADMIN")

                        // ── Availability ──────────────────────────────────────────
                        // GET slots - public (patient browsing)
                        .requestMatchers(HttpMethod.GET, "/api/availability/**", "/availability/**").permitAll()
                        // ADD/UPDATE/DELETE slots - special doctors manage their own
                        .requestMatchers(HttpMethod.POST, "/api/availability/**", "/availability/**").hasAnyRole("ADMIN", "SPECIAL_DOCTOR", "DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/availability/**", "/availability/**").hasAnyRole("ADMIN", "SPECIAL_DOCTOR", "DOCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/availability/**", "/availability/**").hasAnyRole("ADMIN", "SPECIAL_DOCTOR", "DOCTOR")

                        // ── Appointments (booking flow) ───────────────────────────
                        // Book - patient or admin
                        .requestMatchers(HttpMethod.POST, "/api/appointments/book", "/appointments/book").hasAnyRole("PATIENT", "ADMIN")
                        // View own - patient or admin
                        .requestMatchers(HttpMethod.GET, "/api/appointments/user/**", "/appointments/user/**").hasAnyRole("PATIENT", "ADMIN")
                        // Admin all appointments
                        .requestMatchers(HttpMethod.GET, "/api/appointments/admin/**", "/appointments/admin/**").hasRole("ADMIN")
                        // Doctor appointments
                        .requestMatchers(HttpMethod.GET, "/api/doctor/appointments/**", "/doctor/appointments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/doctor/appointments/**", "/doctor/appointments/**").permitAll()
                        // Admin appointments management
                        .requestMatchers("/api/admin/appointments/**", "/admin/appointments/**").permitAll()

                        // ── Chat ──────────────────────────────────────────────────
                        .requestMatchers("/api/chat/**", "/chat/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
