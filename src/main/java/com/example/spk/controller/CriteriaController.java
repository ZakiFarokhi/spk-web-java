package com.example.spk.controller;

import com.example.spk.entity.Criteria;
import com.example.spk.service.CriteriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/criterias")
public class CriteriaController {

    @Autowired
    private CriteriaService criteriaService;

    // List semua criteria
    @GetMapping
    public String listCriteria(Model model, Principal principal) {
        model.addAttribute("username", principal != null?principal.getName(): "Guest");
        model.addAttribute("criterias", criteriaService.findAll());
        return "criterias/index"; // Thymeleaf template: criteria/list.html
    }

    // Form tambah criteria
    @PostMapping("/create")
    public String create(Criteria criteria) {
        criteriaService.save(criteria);
        return "redirect:/criterias"; // Thymeleaf template: criteria/add.html
    }


    // Form edit criteria
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, Criteria criteria) {
        criteriaService.update(id, criteria);
        return "redirect:/criterias"; // Thymeleaf template: criteria/edit.html
    }


    // Hapus criteria
    @GetMapping("/delete/{id}")
    public String deleteCriteria(@PathVariable Long id) {
        criteriaService.deleteById(id);
        return "redirect:/criterias";
    }
}
