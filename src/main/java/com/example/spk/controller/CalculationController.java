package com.example.spk.controller;

import com.example.spk.entity.*;
import com.example.spk.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/calculation")
public class CalculationController {

    @Autowired private AuditorService auditorService;
    @Autowired private AuditorScoreService auditorScoreService;
    @Autowired private CriteriaService criteriaService;
    @Autowired private CalculationService calculationService;
    // CalculationService tidak diperlukan di tahap ini

    /**
     * Menampilkan Matriks Normalisasi (R) - Matriks Keputusan Awal
     * URL: /calculation/normalization
     */
    @GetMapping("/normalization")
    public String showNormalizationMatrix(Model model) {

        // 1. Ambil semua data dasar
        List<Auditor> auditors = auditorService.findAll();
        List<Criteria> criteriaList = criteriaService.findAll();
        List<AuditorScore> allScores = auditorScoreService.findAll();

        // 2. Konversi skor normalisasi ke Map (AuditorId_SubCriteriaId -> AuditorScore)
        // Ini memastikan akses O(1) di Thymeleaf
        Map<String, AuditorScore> normalizedScoreMap = auditorScoreService.convertListToMap(allScores);

        // 3. Ambil semua Sub-Kriteria dalam urutan yang benar (untuk header kolom)
        List<SubCriteria> allSubCriteria = criteriaList.stream()
                // Meratakan semua sub-kriteria dari setiap kriteria
                .flatMap(c -> c.getSubCriteriaList().stream())
                .collect(Collectors.toList());

        model.addAttribute("criterias", criteriaService.findAll());
        model.addAttribute("auditors", auditors);
        model.addAttribute("criteriaList", criteriaList);
        model.addAttribute("allSubCriteria", allSubCriteria);
        model.addAttribute("normalizedScoreMap", normalizedScoreMap);

        return "calculation/normalization_matrix"; // File Thymeleaf di /resources/templates/calculation/
    }

    @GetMapping("/aggregated")
    public String showAggregatedNormalizationMatrix(Model model) {

        List<Auditor> auditors = auditorService.findAll();
        List<Criteria> criteriaList = criteriaService.findAll(); // Hanya Kriteria Utama

        // Hitung Matriks R_c (Hasil Aggregasi)
        Map<String, Double> aggregatedScoreMap = calculationService.calculateAggregatedNormalizationMatrix();

        model.addAttribute("criterias", criteriaList);
        model.addAttribute("auditors", auditors);
        model.addAttribute("criteriaList", criteriaList);
        model.addAttribute("aggregatedScoreMap", aggregatedScoreMap);

        return "calculation/aggregated_matrix"; // Nama file Thymeleaf baru
    }

    @GetMapping("/final_normalized")
    public String showFinalNormalizedMatrix(Model model) {

        List<Auditor> auditors = auditorService.findAll();
        List<Criteria> criteriaList = criteriaService.findAll();

        // Hitung Matriks R_Final (Hasil Aggregasi + Normalisasi Akhir)
        Map<String, Double> finalNormalizedScoreMap = calculationService.calculateFinalNormalizedCriteriaMatrix();

        model.addAttribute("criterias", criteriaList);
        model.addAttribute("auditors", auditors);
        model.addAttribute("criteriaList", criteriaList);
        model.addAttribute("finalNormalizedScoreMap", finalNormalizedScoreMap);

        return "calculation/final_normalized_matrix"; // File Thymeleaf baru
    }

    @GetMapping("/ranking")
    public String showRanking(Model model) {

        // Data dasar untuk header dan tampilan
        List<Criteria> criteriaList = criteriaService.findAll();
        model.addAttribute("criteriaList", criteriaList);
        model.addAttribute("criterias", criteriaList);
        // Cek apakah hasil ranking sudah ada di model (setelah redirect dari POST)
        if (!model.containsAttribute("rankingResults")) {
            // Jika tidak ada hasil (pertama kali akses), inisialisasi list kosong
            model.addAttribute("rankingResults", List.of());
        }

        return "calculation/ranking_result"; // Nama file Thymeleaf Anda
    }

    /**
     * Memicu perhitungan ranking akhir (POST).
     * URL: /calculation/ranking/generate
     */
    @PostMapping("/ranking/generate")
    public String generateRanking(RedirectAttributes redirectAttributes) {

        // PENTING: Lakukan perhitungan skor akhir di service
        List<RankingResult> results = calculationService.calculateFinalRanking();

        // Kirim hasil perhitungan melalui redirect attributes
        // (Flash Attributes agar data tidak hilang setelah redirect)
        redirectAttributes.addFlashAttribute("rankingResults", results);
        redirectAttributes.addFlashAttribute("calculationSuccess", true);

        // Redirect kembali ke halaman GET /ranking untuk menampilkan hasil
        return "redirect:/calculation/ranking";
    }


}