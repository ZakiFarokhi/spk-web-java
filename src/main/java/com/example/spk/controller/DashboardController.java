package com.example.spk.controller;

import com.example.spk.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class DashboardController {

    private final UserRepository userRepository;

    public DashboardController(UserRepository userRepository) { this.userRepository = userRepository; }

    @GetMapping({"/", "dashboard"})
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("username", principal != null ? principal.getName() : "Guest");
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("pageTitle", "Dashboard");
        return "dashboard";
    }
}
