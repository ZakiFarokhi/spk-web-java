package com.example.spk.controller;

import com.example.spk.entity.Auditor;
import com.example.spk.entity.User;
import com.example.spk.service.AuditorService;
import com.example.spk.service.PendidikanService;
import com.example.spk.service.RoleService;
import com.example.spk.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/auditors")
public class AuditorController {

    private final AuditorService auditorService;
    private final PendidikanService pendidikanService;

    public AuditorController(AuditorService auditorService, PendidikanService pendidikanService) {
        this.auditorService = auditorService;
        this.pendidikanService = pendidikanService;
    }

    // ➤ LIST USERS
    @GetMapping()
    public String index(Model model, Principal principal) {
        model.addAttribute("username", principal != null?principal.getName(): "Guest");
        model.addAttribute("auditors", auditorService.findAll());
        model.addAttribute("pendidikan", pendidikanService.findAll());
        return "auditors/index";
    }

    // ➤ PROCESS CREATE
    @PostMapping("/create")
    public String store(Auditor auditor) {
        auditorService.save(auditor);
        return "redirect:/auditors";
    }


    // ➤ PROCESS UPDATE
    @PostMapping("/update/{id}")
    public String update(Auditor auditor) {
        auditorService.save(auditor);
        return "redirect:/auditors";
    }

    // ➤ DELETE
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        auditorService.deleteById(id);
        return "redirect:/auditors";
    }

}
