package com.example.mainservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializationService implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // Automatic admin creation has been removed
        // Admins must be created manually through the admin access page
        // or via the Admin Management section in the admin dashboard
    }
}
