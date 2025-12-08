package com.example.spk.controller;

import com.example.spk.entity.AlternativeScore;
import com.example.spk.entity.Auditor; // Import entity yang diperlukan
import com.example.spk.entity.Crips;
import com.example.spk.entity.SubCriteria;
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

    // -------------------------------------------------------------------------
    // 2. ENDPOINT UPDATE (POST) UNTUK INLINE EDIT
    // -------------------------------------------------------------------------
    @PostMapping("/update/{auditorId}")
    public String updateScores(
            @PathVariable Long auditorId,
            @RequestParam Map<String, String> scores, // Menangkap semua input: scores[SubCriteriaId] = CripsId
            RedirectAttributes ra) {

        Auditor auditor = auditorService.findById(auditorId)
                .orElseThrow(() -> new RuntimeException("Auditor tidak ditemukan"));

        try {
            // Loop melalui Map input dari form
            // Kunci yang dicari adalah "scores[SubCriteriaId]", Nilai adalah CripsId
            for (Map.Entry<String, String> entry : scores.entrySet()) {

                // Hanya proses field yang dimulai dengan "scores[" dan memiliki nilai
                if (entry.getKey().startsWith("scores[") && entry.getValue() != null && !entry.getValue().isEmpty()) {

                    // 1. Ekstrak SubCriteriaId dari kunci (misal: "scores[5]" -> 5)
                    String key = entry.getKey();
                    Long subCriteriaId = Long.valueOf(key.substring(key.indexOf("[") + 1, key.indexOf("]")));

                    Long cripsId = Long.valueOf(entry.getValue());

                    // 2. Dapatkan entity SubCriteria dan Crips
                    SubCriteria subCriteria = subCriteriaService.findById(subCriteriaId)
                            .orElseThrow(() -> new RuntimeException("SubCriteria tidak ditemukan: " + subCriteriaId));
                    Crips crips = cripsService.findById(cripsId)
                            .orElseThrow(() -> new RuntimeException("Crips tidak ditemukan: " + cripsId));

                    // 3. Panggil service untuk menyimpan atau mengupdate skor
                    alternativeScoreService.saveOrUpdateScore(auditor, subCriteria, crips);
                }
            }

            ra.addFlashAttribute("successMessage", "✅ Penilaian untuk " + auditor.getName() + " berhasil diperbarui!");

        } catch (Exception e) {
            // Log error (gunakan logger nyata di aplikasi produksi)
            System.err.println("Gagal memperbarui skor: " + e.getMessage());
            ra.addFlashAttribute("errorMessage", "❌ Gagal memperbarui penilaian: " + e.getMessage());
        }

        return "redirect:/alternative-scores";
    }
}