package com.example.spk.util;

import com.example.spk.entity.*;
import com.example.spk.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner init(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PendidikanRepository pendidikanRepository,
            PasswordEncoder passwordEncoder,
            CriteriaRepository criteriaRepository,
            SubCriteriaRepository subCriteriaRepository,
            CripsRepository cripsRepository) {
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

            // --- CRITERIA SAMPLE
            Criteria c1 = criteriaRepository.findByCode("C1")
                    .orElseGet(() -> {
                        Criteria c = new Criteria();
                        c.setCode("C1");
                        c.setName("Pendidikan");
                        c.setBobot(0.05);
                        c.setIndeks("BENEFIT");
                        return criteriaRepository.save(c);
                    });

            Criteria c2 = criteriaRepository.findByCode("C2")
                    .orElseGet(() -> {
                        Criteria c = new Criteria();
                        c.setCode("C2");
                        c.setName("Hasil Kerja");
                        c.setBobot(0.30);
                        c.setIndeks("BENEFIT");
                        return criteriaRepository.save(c);
                    });

            Criteria c3 = criteriaRepository.findByCode("C3")
                    .orElseGet(() -> {
                        Criteria c = new Criteria();
                        c.setCode("C3");
                        c.setName("Perilaku Kerja");
                        c.setBobot(0.25);
                        c.setIndeks("BENEFIT");
                        return criteriaRepository.save(c);
                    });

            Criteria c4 = criteriaRepository.findByCode("C4")
                    .orElseGet(() -> {
                        Criteria c = new Criteria();
                        c.setCode("C4");
                        c.setName("Kompetensi");
                        c.setBobot(0.20);
                        c.setIndeks("BENEFIT");
                        return criteriaRepository.save(c);
                    });

            Criteria c5 = criteriaRepository.findByCode("C5")
                    .orElseGet(() -> {
                        Criteria c = new Criteria();
                        c.setCode("C5");
                        c.setName("SKP");
                        c.setBobot(0.04);
                        c.setIndeks("BENEFIT");
                        return criteriaRepository.save(c);
                    });

            Criteria c6 = criteriaRepository.findByCode("C6")
                    .orElseGet(() -> {
                        Criteria c = new Criteria();
                        c.setCode("C6");
                        c.setName("Angka Kredit");
                        c.setBobot(0.04);
                        c.setIndeks("BENEFIT");
                        return criteriaRepository.save(c);
                    });

            Criteria c7 = criteriaRepository.findByCode("C7")
                    .orElseGet(() -> {
                        Criteria c = new Criteria();
                        c.setCode("C7");
                        c.setName("Penilaian Atasan");
                        c.setBobot(0.12);
                        c.setIndeks("BENEFIT");
                        return criteriaRepository.save(c);
                    });

            // --- SUB CRITERIA SAMPLE
            SubCriteria sc1 = subCriteriaRepository.findByCode("C2.1")
                    .orElseGet(() -> {
                        SubCriteria s = new SubCriteria();
                        s.setCode("C2.1");
                        s.setName("Lama Bekerja");
                        s.setCriteria(c2);
                        return subCriteriaRepository.save(s);
                    });

            SubCriteria sc2 = subCriteriaRepository.findByCode("C1.1")
                    .orElseGet(() -> {
                        SubCriteria s = new SubCriteria();
                        s.setCode("C1.1");
                        s.setName("SMA/SMK");
                        s.setCriteria(c1);
                        return subCriteriaRepository.save(s);
                    });
            SubCriteria sc3 = subCriteriaRepository.findByCode("C1.2")
                    .orElseGet(() -> {
                        SubCriteria s = new SubCriteria();
                        s.setCode("C1.2");
                        s.setName("Diploma");
                        s.setCriteria(c1);
                        return subCriteriaRepository.save(s);
                    });
            SubCriteria sc4 = subCriteriaRepository.findByCode("C1.3")
                    .orElseGet(() -> {
                        SubCriteria s = new SubCriteria();
                        s.setCode("C1.3");
                        s.setName("S1");
                        s.setCriteria(c1);
                        return subCriteriaRepository.save(s);
                    });
            SubCriteria sc5 = subCriteriaRepository.findByCode("C1.4")
                    .orElseGet(() -> {
                        SubCriteria s = new SubCriteria();
                        s.setCode("C1.4");
                        s.setName("S2");
                        s.setCriteria(c1);
                        return subCriteriaRepository.save(s);
                    });
            SubCriteria sc6 = subCriteriaRepository.findByCode("C1.5")
                    .orElseGet(() -> {
                        SubCriteria s = new SubCriteria();
                        s.setCode("C1.5");
                        s.setName("S3");
                        s.setCriteria(c1);
                        return subCriteriaRepository.save(s);
                    });

            // --- CRIPS SAMPLE
            if (cripsRepository.findBySubCriteria(sc1).isEmpty()) {
                Crips c = new Crips();
                c.setDescription("0-1 Tahun");
                c.setNilai(10.0);
                c.setSubCriteria(sc1);
                cripsRepository.save(c);

                Crips c2s = new Crips();
                c2s.setDescription("2-3 Tahun");
                c2s.setNilai(20.0);
                c2s.setSubCriteria(sc1);
                cripsRepository.save(c2s);
            }

            System.out.println("✔ DATA CRITERIA–SUBCRITERIA–CRIPS LOADED");
        };
    }
}
