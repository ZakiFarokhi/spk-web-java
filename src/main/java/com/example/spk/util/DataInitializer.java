package com.example.spk.util;

import com.example.spk.entity.Pendidikan;
import com.example.spk.entity.Role;
import com.example.spk.entity.User;
import com.example.spk.repository.PendidikanRepository;
import com.example.spk.repository.RoleRepository;
import com.example.spk.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner init(RoleRepository roleRepository, UserRepository userRepository, PendidikanRepository pendidikanRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // ----------------------------
            // 1. Create Roles if not exist
            // ----------------------------
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ADMIN")));

            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> roleRepository.save(new Role("USER")));

            pendidikanRepository.findByName("SMA/SMK").orElseGet(()-> pendidikanRepository.save(new Pendidikan("SMA/SMK")));
            pendidikanRepository.findByName("Diploma").orElseGet(()-> pendidikanRepository.save(new Pendidikan("Diploma")));
            pendidikanRepository.findByName("S1").orElseGet(()-> pendidikanRepository.save(new Pendidikan("S1")));
            pendidikanRepository.findByName("S2").orElseGet(()-> pendidikanRepository.save(new Pendidikan("S2")));
            pendidikanRepository.findByName("S3").orElseGet(()-> pendidikanRepository.save(new Pendidikan("S3")));


            // ----------------------------
            // 2. Create admin user if not exist
            // ----------------------------
            Optional<User> existingAdmin = userRepository.findByUsername("admin");

            if (existingAdmin.isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123")); // hashed password
                admin.setEnabled(true);
                admin.setRole(adminRole);
                userRepository.save(admin);

                System.out.println("Admin user created with username=admin and password=admin123");
            } else {
                System.out.println("Admin user already exists.");
            }

            // ----------------------------
            // 3. Optional: Create sample regular user
            // ----------------------------
            Optional<User> existingUser = userRepository.findByUsername("user");
            if (existingUser.isEmpty()) {
                User user = new User();
                user.setUsername("user");
                user.setEmail("user@example.com");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setEnabled(true);
                user.setRole(userRole);
                userRepository.save(user);

                System.out.println("User created with username=user and password=user123");
            }
        };
    }
}
