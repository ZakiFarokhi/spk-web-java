package com.example.spk.controller;

import com.example.spk.entity.*;
import com.example.spk.service.AlternativeScoreService;
import com.example.spk.service.AuditorService;
import com.example.spk.service.CripsService;
import com.example.spk.service.CriteriaService;
import com.example.spk.service.SubCriteriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; // Menggunakan semua annotation dari rest
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/alternative-scores") // Perbaiki: Gunakan underscore agar konsisten dengan view
public class AlternativeScoreController {

    private final CriteriaService criteriaService;
    private final SubCriteriaService subCriteriaService;
    private final CripsService cripsService;
    private final AuditorService auditorService;
    private final AlternativeScoreService alternativeScoreService;

    // Constructor Injection (Sudah bagus dan dipertahankan)
    @Autowired
    public AlternativeScoreController(
            CriteriaService criteriaService,
            SubCriteriaService subCriteriaService,
            CripsService cripsService,
            AuditorService auditorService,
            AlternativeScoreService alternativeScoreService) {
        this.criteriaService = criteriaService;
        this.subCriteriaService = subCriteriaService;
        this.cripsService = cripsService;
        this.auditorService = auditorService;
        this.alternativeScoreService = alternativeScoreService;
    }

    // -------------------------------------------------------------------------
    // 1. ENDPOINT VIEW (GET)
    // -------------------------------------------------------------------------
    @GetMapping
    public String listAltenativeScores(Model model, Principal principal) {

        // 1. Ambil data skor
        List<AlternativeScore> scores = alternativeScoreService.findAllScores();

        // 2. Konversi List skor menjadi Map (Untuk akses cepat di Thymeleaf)
        Map<String, AlternativeScore> scoreMap = convertScoresToMap(scores);

        // 3. Masukkan semua data yang dibutuhkan ke Model
        model.addAttribute("username", principal != null ? principal.getName() : "Guest");
        model.addAttribute("subCriterias", subCriteriaService.findAll());
        model.addAttribute("criterias", criteriaService.findAllWithSubCriterias()); // Pastikan Kriteria dimuat dengan SubKriteria
        model.addAttribute("cripsList", cripsService.findAll());
        model.addAttribute("auditors", auditorService.findAll());
        model.addAttribute("scoreMap", scoreMap);

        return "alternative_scores/index";
    }

    // Helper method untuk konversi List Skor menjadi Map
    private Map<String, AlternativeScore> convertScoresToMap(List<AlternativeScore> scores) {
        Map<String, AlternativeScore> scoreMap = new HashMap<>();
        for (AlternativeScore score : scores) {
            String key = score.getAuditor().getId() + "_" + score.getSubCriteria().getId();
            scoreMap.put(key, score);
        }
        return scoreMap;
    }

    // di AlternativeScoreController.java

    @GetMapping("/input")
    public String showInputPage(
            Model model,
            // Parameter opsional dari tombol edit di tabel
            @RequestParam(required = false) Long auditorId) {

        List<Auditor> auditors = auditorService.findAll();
        List<Criteria> criteria = criteriaService.findAll();

        model.addAttribute("auditors", auditors);
        model.addAttribute("selectedAuditorId", auditorId); // Kirim ID yang dipilih ke view

        return "alternative-scores/score-input-page";
    }

    @GetMapping("/input-single")
    public String showSingleInputPage(
            @RequestParam("auditorId") Long auditorId,
            @RequestParam("criteriaId") Long criteriaId,
            Model model) {

        // 1. Muat Auditor spesifik
        Auditor auditor = auditorService.findById(auditorId)
                .orElseThrow(() -> new IllegalArgumentException("Auditor tidak ditemukan."));

        // 2. Muat Kriteria spesifik (termasuk Sub Kriteria dan Crips-nya)
        Criteria criteria = criteriaService.findCriteriaByIdWithDetails(criteriaId) // Asumsi ada method ini
                .orElseThrow(() -> new IllegalArgumentException("Kriteria tidak ditemukan."));

        // 3. Muat skor yang sudah ada untuk Auditor dan Kriteria ini
        Map<String, AlternativeScore> scoreMap = alternativeScoreService.findAllScoresAsMapForCriteriaAndAuditor(
                auditorId, criteria.getSubCriteriaList()); // Asumsi ada method service yang spesifik

        model.addAttribute("auditor", auditor);
        model.addAttribute("criteria", criteria);
        model.addAttribute("scoreMap", scoreMap);

        // Gunakan nama file baru
        return "alternative/scores/score_single_input";
    }

    @PostMapping("/save-raw-scores-criteria")
    public String saveRawScoresPerCriteria(
            @RequestParam("auditorId") Long auditorId,
            @RequestParam("criteriaId") Long criteriaId,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes) {

        try {
            // 1. Ekstrak skor yang relevan
            Map<Long, Double> rawScores = alternativeScoreService.extractRawScores(allParams);

            // 2. Simpan/Update (Controller tidak perlu tahu bahwa ini hanya satu kriteria)
            alternativeScoreService.saveOrUpdateRawScores(auditorId, rawScores);
            // Note: rawScores di sini hanya berisi SubCriteriaId yang termasuk dalam CriteriaId yang disubmit.

            redirectAttributes.addFlashAttribute("successMessage",
                    "Nilai Kriteria " + criteriaId + " untuk Auditor " + auditorId + " berhasil disimpan/diperbarui!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Gagal menyimpan nilai: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Terjadi kesalahan sistem saat menyimpan nilai. Periksa log server.");
            e.printStackTrace();
        }

        return "redirect:/alternative-scores/input";
    }

}