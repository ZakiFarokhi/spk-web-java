package com.example.spk.controller;

import com.example.spk.entity.Criteria;
import com.example.spk.service.CriteriaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
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
    public String create(Criteria criteria, RedirectAttributes redirectAttributes) {
        try {
            criteriaService.save(criteria);
            redirectAttributes.addFlashAttribute("success", "Kriteria berhasil ditambahkan");
        }catch (RuntimeException e){
            redirectAttributes.addAttribute("error", e.getMessage());
        }

        return "redirect:/criterias"; // Thymeleaf template: criteria/add.html
    }


    // Form edit criteria
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, Criteria criteria, RedirectAttributes redirectAttributes) {
        try {
            criteriaService.update(id, criteria);
            redirectAttributes.addFlashAttribute("success", "Kriteria berhasil diubah");
        }catch (RuntimeException e){
            redirectAttributes.addAttribute("error", e.getMessage());
        }

        return "redirect:/criterias"; // Thymeleaf template: criteria/edit.html
    }


    // Hapus criteria
    @GetMapping("/delete/{id}")
    public String deleteCriteria(@PathVariable Long id) {
        criteriaService.deleteById(id);
        return "redirect:/criterias";
    }

    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=data_kriteria.xlsx";
        response.setHeader(headerKey, headerValue);

        criteriaService.exportToExcel(response);
    }

    @GetMapping("/export-pdf")
    public void exportToPdf(HttpServletResponse response) {
        try {
            // Mengatur metadata response untuk file PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Laporan_Kriteria_SPK.pdf");

            criteriaService.exportToPdf(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
