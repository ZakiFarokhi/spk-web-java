package com.example.spk.controller;

import com.example.spk.entity.Crips;
import com.example.spk.entity.Criteria;
import com.example.spk.entity.AuditorScore;
import com.example.spk.repository.AuditorRepository; // BARU: Tambahkan import
import com.example.spk.repository.SubCriteriaRepository; // BARU: Tambahkan import
import com.example.spk.repository.CriteriaRepository;
import com.example.spk.repository.CripsRepository;
import com.example.spk.service.AuditorScoreService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/auditor-scores")
public class AuditorScoreController {

    @Autowired private AuditorScoreService scoreService;
    @Autowired private CriteriaRepository criteriaRepository;
    @Autowired private CripsRepository cripsRepository;

    // INJEKSI REPOSITORY YANG HILANG
    @Autowired private AuditorRepository auditorRepository;
    @Autowired private SubCriteriaRepository subCriteriaRepository;

    /**
     * Menampilkan halaman input nilai mentah untuk kriteria tertentu.
     * URL: /auditor-scores/{id}
     */
    @GetMapping("/{criteriaId}")
    public String showScoreInput(@PathVariable Long criteriaId, Model model, Principal principal) {
          model.addAttribute("username", principal != null ? principal.getName() : "Guest");
          model.addAttribute("criterias", criteriaRepository.findAll());
        Criteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new RuntimeException("Kriteria tidak ditemukan."));
        model.addAttribute("criteria", criteria);
        model.addAttribute("criteriaId", criteriaId);

        boolean isGenerated = scoreService.isDataGenerated(criteriaId);
        model.addAttribute("isGenerated", isGenerated);

        // --- PENYEDIAAN DATA WAJIB ---
        // 1. Mengambil semua Auditor (Perbaikan 2)
        model.addAttribute("auditors", auditorRepository.findAll());

        // 2. Mengambil Sub-Kriteria untuk header tabel (Pastikan method repository benar)
        model.addAttribute("subCriteriaList", subCriteriaRepository.findByCriteriaId(criteriaId));

        // 3. Mengambil Crips untuk dropdown (diperlukan saat update diimplementasikan)
        model.addAttribute("allCrips", cripsRepository.findAll());
//        List<Crips> listCrips = cripsRepository.findAll();
//        listCrips.forEach(crips -> {
//            crips.getSubCriteria().get
//        });

        if (isGenerated) {
            // Panggil method service yang benar (Perbaikan 4)
            List<AuditorScore> scores = scoreService.getScoresByCriteria(criteriaId);
            model.addAttribute("scoreMap", scoreService.convertListToMap(scores));
        } else {
            model.addAttribute("scoreMap", Collections.emptyMap());
        }

        return "/auditor_scores/index.html";
    }

    /**
     * Menangani request POST saat tombol 'Generate' diklik.
     * URL: /auditor-scores/generate/{id}
     */
    @PostMapping("/generate/{criteriaId}")
    public String generateDefaultScores(@PathVariable Long criteriaId, RedirectAttributes ra) {
        try {
            scoreService.generateDefaultScores(criteriaId);
            ra.addFlashAttribute("successMessage", "Kerangka penilaian berhasil dibuat!");

            // REVISI: Redirect ke URL yang benar /auditor-scores/{criteriaId} (Perbaikan 3)
            return "redirect:/auditor-scores/" + criteriaId;
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Gagal membuat data: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/auditor-scores/" + criteriaId;
        }
    }

    // Tambahkan method @PostMapping("/update") jika Anda mengimplementasikan update AJAX.
    @PostMapping("/update-row")
    public String updateAuditorRowScores(
            @RequestParam("auditorId") Long auditorId,
            @RequestParam("criteriaId") Long criteriaId,
            @RequestParam Map<String, String> cripsIds, // Mengambil Map<scoreId, cripsId>
            @RequestParam Map<String, String> scoreIds, // Mengambil semua hidden field scoreIds[...]
            RedirectAttributes redirectAttributes) {

        try {
            Map<Long, Long> scoresToUpdate = new HashMap<>();

            // Iterasi melalui ID skor yang lama (hidden field scoreIds[...])
            scoreIds.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("scoreIds["))
                    .forEach(entry -> {
                        try {
                            // Nilai dari hidden field adalah Score ID (e.g., "123")
                            Long scoreId = Long.parseLong(entry.getValue());

                            // Dapatkan key Crips yang sesuai (e.g., "cripsIds[123]")
                            String cripsKey = "cripsIds[" + scoreId + "]";
                            String newCripsIdString = cripsIds.get(cripsKey);

                            if (newCripsIdString != null && !newCripsIdString.isEmpty()) {
                                // Lakukan konversi String ke Long secara eksplisit
                                Long newCripsId = Long.parseLong(newCripsIdString);

                                // Map<AlternativeScoreId, NewCripsId>
                                scoresToUpdate.put(scoreId, newCripsId);
                            }
                            // Jika newCripsIdString kosong, itu berarti opsi "-- Pilih Nilai --" terpilih.

                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing ID dalam loop update: " + e.getMessage());
                        }
                    });

            // Panggil Service Layer
            scoreService.updateScoresByCripsId(scoresToUpdate);

            redirectAttributes.addFlashAttribute("successMessage", "Penilaian Auditor " + auditorId + " berhasil diperbarui.");

        } catch (Exception e) {
            System.err.println("Gagal mengupdate skor: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal mengupdate skor: " + e.getMessage());
        }

        // Redirect kembali ke halaman yang sama (penilaian kriteria yang sedang aktif)
        return "redirect:/auditor-scores/" + criteriaId;
    }

    @GetMapping("/export/{criteriaId}")
    public void exportToExcel(@PathVariable Long criteriaId, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // Nama file dinamis berdasarkan ID Kriteria
        String headerValue = "attachment; filename=konversi_kriteria_" + criteriaId + ".xlsx";
        response.setHeader("Content-Disposition", headerValue);

        scoreService.exportToExcel(criteriaId, response);
    }

}