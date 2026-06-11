package com.example.digitalwalletsystem.config;
import com.example.digitalwalletsystem.model.User;
import com.example.digitalwalletsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
@Configuration
public class DatabaseLoader {
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@admin.com";


            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                User admin = new User();
                admin.setName("System Administrator");
                admin.setEmail(adminEmail);


                admin.setPassword(passwordEncoder.encode("AdminPass123"));


                admin.setRole("ADMIN");
                admin.setStatus("ACTIVE");
                admin.setCreatedAt(LocalDateTime.now());


                userRepository.save(admin);

                System.out.println(">> [DatabaseLoader] The administrator account has been successfully created!");
            } else {
                System.out.println(">> [DatabaseLoader] The administrator already exists in the database. Skipping creation.");
            }
        };
    }
}
