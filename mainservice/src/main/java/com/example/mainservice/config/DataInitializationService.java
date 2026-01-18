package com.example.mainservice.config;

import com.example.mainservice.entity.Admin;
import com.example.mainservice.repository.AdminRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializationService implements CommandLineRunner {

    private final AdminRepo adminRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.username:admin}")
    private String defaultUsername;

    @Value("${admin.default.password:admin123}")
    private String defaultPassword;

    @Value("${admin.default.email:admin@hospital.com}")
    private String defaultEmail;

    @Value("${admin.default.name:System Administrator}")
    private String defaultName;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin account if it doesn't exist
        if (!adminRepo.existsByUsername(defaultUsername)) {
            Admin defaultAdmin = Admin.builder()
                    .username(defaultUsername)
                    .password(passwordEncoder.encode(defaultPassword))
                    .email(defaultEmail)
                    .name(defaultName)
                    .build();
            adminRepo.save(defaultAdmin);
            System.out.println("==========================================");
            System.out.println("Default admin account created:");
            System.out.println("Username: " + defaultUsername);
            System.out.println("Password: " + defaultPassword);
            System.out.println("Email: " + defaultEmail);
            System.out.println("Name: " + defaultName);
            System.out.println("Please change the password after first login!");
            System.out.println("==========================================");
        } else {
            System.out.println("Admin account already exists. Skipping creation.");
        }
    }
}
