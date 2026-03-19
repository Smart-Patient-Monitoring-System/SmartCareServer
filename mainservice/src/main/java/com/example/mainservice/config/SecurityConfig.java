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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**", "/auth/**").permitAll()
                        .requestMatchers("/api/admin/**", "/admin/**").permitAll()
                        .requestMatchers("/api/doctor/**", "/doctor/**").permitAll()
                        .requestMatchers("/api/patient/**", "/patient/**").permitAll()
                        .requestMatchers("/api/pendingdoctor/**", "/pendingdoctor/**").permitAll()
                        .requestMatchers("/api/dashboard/**", "/dashboard/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/payments/pay/**", "/payments/pay/**").permitAll()
                        .requestMatchers("/api/payments/notify", "/payments/notify").permitAll()
                        .requestMatchers("/ws/**", "/ws").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/doctors/**", "/doctors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/appointment-types/**", "/appointment-types/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/availability/doctor/**", "/availability/doctor/**").permitAll()
                        .requestMatchers("/api/availability/fix-db", "/availability/fix-db").permitAll()
                        .requestMatchers("/api/doctor-notes/**", "/doctor-notes/**").authenticated()
                        .requestMatchers("/api/vital-signs/**", "/vital-signs/**").hasAnyRole("PATIENT", "ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/appointments/book", "/appointments/book").hasAnyRole("PATIENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/appointments/user/**", "/appointments/user/**").hasAnyRole("PATIENT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/doctors/**", "/doctors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/doctors/**", "/doctors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/doctors/**", "/doctors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/appointment-types/**", "/appointment-types/**").hasRole("ADMIN")
                        .requestMatchers("/api/chat/**", "/chat/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
