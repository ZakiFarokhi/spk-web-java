package com.example.spk.controller;

import com.example.spk.entity.Crips;
import com.example.spk.entity.Criteria;
import com.example.spk.entity.SubCriteria;
import com.example.spk.service.CripsService;
import com.example.spk.service.CriteriaService;
import com.example.spk.service.SubCriteriaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
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
    public String create(SubCriteria subCriteria, RedirectAttributes redirectAttributes) {
        try{
            subCriteriaService.save(subCriteria);
            redirectAttributes.addFlashAttribute("success", "SubCriteria Berhasil ditambahkan");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }

        return "redirect:/sub_criterias"; // Thymeleaf template: criteria/add.html
    }


    // Form edit criteria
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, SubCriteria subCriteria, RedirectAttributes redirectAttributes) {
        try{
            subCriteriaService.update(id, subCriteria);
            redirectAttributes.addFlashAttribute("success", "SubCriteria Berhasil diubah");
        }
        catch (Exception e){
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        return "redirect:/sub_criterias"; // Thymeleaf template: criteria/edit.html
    }


    // Hapus criteria
    @GetMapping("/delete/{id}")
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

    @GetMapping("/crips/delete/{id}")
    public String deleteCrips(@PathVariable Long id) {
        cripsService.deleteById(id);
        return "redirect:/sub_criterias";
    }

    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=data_sub_kriteria_crips.xlsx";
        response.setHeader(headerKey, headerValue);

        subCriteriaService.exportToExcel(response);
    }

    @GetMapping("/export-pdf")
    public void exportPdf(HttpServletResponse response) {
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Laporan_SubKriteria_SPK.pdf");
            subCriteriaService.exportToPdf(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
