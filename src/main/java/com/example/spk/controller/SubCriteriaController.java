package com.example.spk.controller;

import com.example.spk.entity.Crips;
import com.example.spk.entity.Criteria;
import com.example.spk.entity.SubCriteria;
import com.example.spk.service.CripsService;
import com.example.spk.service.CriteriaService;
import com.example.spk.service.SubCriteriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/sub_criterias")
public class SubCriteriaController {

    private final CriteriaService criteriaService;
    private final SubCriteriaService subCriteriaService;
    private final CripsService cripsService;

    public SubCriteriaController(CriteriaService criteriaService, SubCriteriaService subCriteriaService, CripsService cripsService) {
        this.criteriaService = criteriaService;
        this.subCriteriaService = subCriteriaService;
        this.cripsService = cripsService;
    }

    // List semua criteria
    @GetMapping
    public String listCriteria(Model model, Principal principal) {
        model.addAttribute("username", principal != null?principal.getName(): "Guest");
        model.addAttribute("sub_criterias", subCriteriaService.findAll());
        model.addAttribute("criterias", criteriaService.findAll());
        return "sub_criterias/index"; // Thymeleaf template: criteria/list.html
    }

    // Form tambah criteria
    @PostMapping("/create")
    public String create(SubCriteria subCriteria) {
        subCriteriaService.save(subCriteria);
        return "redirect:/sub_criterias"; // Thymeleaf template: criteria/add.html
    }


    // Form edit criteria
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, SubCriteria subCriteria) {
        subCriteriaService.update(id, subCriteria);
        return "redirect:/sub_criterias"; // Thymeleaf template: criteria/edit.html
    }


    // Hapus criteria
    @PostMapping("/delete/{id}")
    public String deleteCriteria(@PathVariable Long id) {
        subCriteriaService.deleteById(id);
        return "redirect:/sub_criterias";
    }

    //Crips
    @PostMapping("/crips/create")
    public String cripsCreate(Crips crips) {
        cripsService.save(crips);
        return "redirect:/sub_criterias"; // Thymeleaf template: criteria/add.html
    }

    @PostMapping("/crips/update/{id}")
    public String updateCrips(@PathVariable Long id, Crips crips) {
        cripsService.update(id, crips);
        return "redirect:/sub_criterias"; // Thymeleaf template: criteria/edit.html
    }
}
