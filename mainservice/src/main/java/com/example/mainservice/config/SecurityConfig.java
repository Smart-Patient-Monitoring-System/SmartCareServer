package com.example.mainservice.config;

import com.example.mainservice.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Comma-separated origins (can be overridden in application.properties or env)
    // Example: cors.allowed.origins=http://localhost:5173,http://localhost:3000
    @Value("${spring.web.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")

    private String allowedOrigins;

    public SecurityConfig(
            @Lazy UserDetailsService userDetailsService,
            @Lazy JwtAuthenticationFilter jwtAuthenticationFilter) {
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
     * CORS for REST endpoints (handled by Spring Security)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Trim spaces and ignore empty values
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // -------- PUBLIC --------
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers("/api/doctor/**").permitAll()
                        .requestMatchers("/api/patient/**").permitAll()
                        .requestMatchers("/api/pendingdoctor/**").permitAll()
                        .requestMatchers("/api/dashboard/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/payments/pay/**").permitAll()
                        .requestMatchers("/api/payments/notify").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/payments/dev-success-all").permitAll()
                        .requestMatchers("/ws/**", "/ws").permitAll()

                        .requestMatchers("/error").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // Booking UI needs these even before login (or at least for PATIENT)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/doctors/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/appointment-types/**")
                        .permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/availability/doctor/**")
                        .permitAll()
                        .requestMatchers("/api/availability/fix-db").permitAll()

                        // -------- DOCTOR (logged in) --------
                        .requestMatchers("/api/doctor-notes/**").authenticated()
                        .requestMatchers("/api/doctor/**").hasRole("DOCTOR")

                        // -------- PATIENT (logged in) --------
                        .requestMatchers("/api/vital-signs/**").hasAnyRole("PATIENT", "ADMIN", "DOCTOR")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/appointments/book")
                        .hasAnyRole("PATIENT", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/appointments/user/**")
                        .hasAnyRole("PATIENT", "ADMIN")

                        // -------- ADMIN ONLY --------
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/doctors/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/doctors/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/doctors/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/appointment-types/**")
                        .hasRole("ADMIN")

                        // Chat
                        .requestMatchers("/api/chat/**").authenticated()

                        // Everything else MUST be authenticated
                        .anyRequest().authenticated())

                // JWT filter before Spring's default auth filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
