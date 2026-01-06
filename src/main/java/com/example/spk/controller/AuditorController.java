package com.example.spk.controller;

import com.example.spk.entity.Auditor;
import com.example.spk.service.*;
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
    private final CriteriaService criteriaService;
    private final AuditorScoreService auditorScoreService;

    public AuditorController(AuditorService auditorService, PendidikanService pendidikanService, CriteriaService criteriaService, AuditorScoreService auditorScoreService) {
        this.auditorService = auditorService;
        this.pendidikanService = pendidikanService;
        this.criteriaService = criteriaService;
        this.auditorScoreService = auditorScoreService;
    }

    // ➤ LIST USERS
    @GetMapping()
    public String index(Model model, Principal principal) {
        model.addAttribute("username", principal != null?principal.getName(): "Guest");
        model.addAttribute("auditors", auditorService.findAll());
        model.addAttribute("pendidikan", pendidikanService.findAll());
        model.addAttribute("criterias", criteriaService.findAll());

        return "auditors/index";
    }

    // ➤ PROCESS CREATE
    @PostMapping("/create")
    public String store(Auditor auditor) {
        Auditor newAuditor = auditorService.save(auditor);
        auditorScoreService.generateDefaultScoreByAuditorId(newAuditor.getId());
        return "redirect:/auditors";
    }


    // ➤ PROCESS UPDATE
    @PostMapping("/update/{id}")
    public String update(Auditor auditor) {
        auditorService.save(auditor);
        return "redirect:/auditors";
    }

    // ➤ DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        auditorService.deleteById(id);
        return "redirect:/auditors";
    }

}
