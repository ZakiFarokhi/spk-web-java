package com.example.spk.util;

import com.example.spk.entity.Role;
import com.example.spk.entity.User;
import com.example.spk.service.RoleService;
import com.example.spk.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner init(RoleService roleService, UserService userService, PasswordEncoder passwordEncoder) {
        return args -> {

            Role adminRole = roleService.findByName("ADMIN");
            if (adminRole == null) adminRole = roleService.save(new Role("ADMIN"));

            Role userRole = roleService.findByName("USER");
            if (userRole == null) userRole = roleService.save(new Role("USER"));

            User admin = userService.findById(1L);
            if (admin == null) {
                admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEnabled(true);
                admin.setRole(adminRole);
                userService.save(admin);
            }
        };
    }
}
