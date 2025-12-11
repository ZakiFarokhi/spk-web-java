package com.example.spk.controller;

import com.example.spk.repository.AuditorRepository;
import com.example.spk.repository.CriteriaRepository;
import com.example.spk.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final AuditorRepository auditorRepository;
    private final CriteriaRepository criteriaRepository;

    public DashboardController(UserRepository userRepository, AuditorRepository auditorRepository, CriteriaRepository criteriaRepository) {
        this.userRepository = userRepository;
        this.auditorRepository = auditorRepository;
        this.criteriaRepository = criteriaRepository;
    }

    @GetMapping({"/", "dashboard"})
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("username", principal != null ? principal.getName() : "Guest");
        model.addAttribute("AuditorCount", auditorRepository.count());
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("criterias", criteriaRepository.findAll()); // Pastikan Kriteria dimuat dengan SubKriteria
        return "dashboard";
    }
}
