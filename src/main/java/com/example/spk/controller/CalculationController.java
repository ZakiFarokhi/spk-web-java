package com.example.spk.controller;

import com.example.spk.entity.*;
import com.example.spk.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.security.Principal;
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
    @Autowired private ExcelExportService excelExportService;
    // CalculationService tidak diperlukan di tahap ini

    private void setupResponse(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerValue = "attachment; filename=" + filename + "_" + System.currentTimeMillis() + ".xlsx";
        response.setHeader("Content-Disposition", headerValue);
    }

    /**
     * Menampilkan Matriks Normalisasi (R) - Matriks Keputusan Awal
     * URL: /calculation/normalization
     */
    @GetMapping("/normalization")
    public String showNormalizationMatrix(Model model, Principal principal) {

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

        model.addAttribute("username", principal != null ? principal.getName() : "Guest");
        model.addAttribute("criterias", criteriaService.findAll());
        model.addAttribute("auditors", auditors);
        model.addAttribute("criteriaList", criteriaList);
        model.addAttribute("allSubCriteria", allSubCriteria);
        model.addAttribute("normalizedScoreMap", normalizedScoreMap);

        return "calculation/normalization_matrix"; // File Thymeleaf di /resources/templates/calculation/
    }

    @GetMapping("/normalization/export")
    public void exportNormalizationMatrix(HttpServletResponse response) throws IOException {
        List<Auditor> auditors = auditorService.findAll();
        List<Criteria> criteriaList = criteriaService.findAll();
        List<AuditorScore> allScores = auditorScoreService.findAll();

        Map<String, AuditorScore> normalizedScoreMap = auditorScoreService.convertListToMap(allScores);

        List<SubCriteria> allSubCriteria = criteriaList.stream()
                .flatMap(c -> c.getSubCriteriaList().stream())
                .collect(Collectors.toList());

        setupResponse(response, "Normalization_Matrix_Sub_Criteria");
        // try-with-resources memastikan workbook ditutup setelah write
        try (XSSFWorkbook workbook = excelExportService.exportNormalizationMatrix(auditors, allSubCriteria, normalizedScoreMap)) {
            workbook.write(response.getOutputStream());
        }
        //return "redirect:/calculation/normalization";
    }



    @GetMapping("/aggregated")
    public String showAggregatedNormalizationMatrix(Model model, Principal principal) {

        List<Auditor> auditors = auditorService.findAll();
        List<Criteria> criteriaList = criteriaService.findAll(); // Hanya Kriteria Utama

        // Hitung Matriks R_c (Hasil Aggregasi)
        Map<String, Double> aggregatedScoreMap = calculationService.calculateAggregatedNormalizationMatrix();

        model.addAttribute("username", principal != null ? principal.getName() : "Guest");
        model.addAttribute("criterias", criteriaList);
        model.addAttribute("auditors", auditors);
        model.addAttribute("criteriaList", criteriaList);
        model.addAttribute("aggregatedScoreMap", aggregatedScoreMap);

        return "calculation/aggregated_matrix"; // Nama file Thymeleaf baru
    }

    @GetMapping("/aggregated/export")
    public void exportAggregatedMatrix(HttpServletResponse response) throws IOException {
        List<Auditor> auditors = auditorService.findAll();
        List<Criteria> criteriaList = criteriaService.findAll();

        Map<String, Double> aggregatedScoreMap = calculationService.calculateAggregatedNormalizationMatrix();

        setupResponse(response, "Aggregated_Matrix_Cj_norm");
        try (XSSFWorkbook workbook = excelExportService.exportCriteriaMatrix(auditors, criteriaList, aggregatedScoreMap, "Aggregated Scores C_j,norm")) {
            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/final_normalized")
    public String showFinalNormalizedMatrix(Model model,  Principal principal) {

        List<Auditor> auditors = auditorService.findAll();
        List<Criteria> criteriaList = criteriaService.findAll();

        // Hitung Matriks R_Final (Hasil Aggregasi + Normalisasi Akhir)
        Map<String, Double> finalNormalizedScoreMap = calculationService.calculateFinalNormalizedCriteriaMatrix();

        model.addAttribute("username", principal != null ? principal.getName() : "Guest");
        model.addAttribute("criterias", criteriaList);
        model.addAttribute("auditors", auditors);
        model.addAttribute("criteriaList", criteriaList);
        model.addAttribute("finalNormalizedScoreMap", finalNormalizedScoreMap);

        return "calculation/final_normalized_matrix"; // File Thymeleaf baru
    }

    @GetMapping("/final-normalized/export")
    public void exportFinalNormalizedMatrix(HttpServletResponse response) throws IOException {
        List<Auditor> auditors = auditorService.findAll();
        List<Criteria> criteriaList = criteriaService.findAll();

        Map<String, Double> finalNormalizedMap = calculationService.calculateFinalNormalizedCriteriaMatrix();

        setupResponse(response, "Final_Normalized_Matrix_Rij");
        try (XSSFWorkbook workbook = excelExportService.exportCriteriaMatrix(auditors, criteriaList, finalNormalizedMap, "Final Normalized R_ij")) {
            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/ranking")
    public String showAndGenerateRanking(Model model, Principal principal) {

        // 1. Ambil Data Dasar untuk header dan tampilan (Kriteria)
        List<Criteria> criteriaList = criteriaService.findAll();
        model.addAttribute("username", principal != null ? principal.getName() : "Guest");
        model.addAttribute("criteriaList", criteriaList);
        model.addAttribute("criterias", criteriaList);

        // 2. Memicu Perhitungan Ranking
        // PENTING: Panggil service untuk mendapatkan hasil ranking terbaru
        try {
            List<RankingResult> rankingResults = calculationService.calculateFinalRanking();

            // 3. Tambahkan Hasil ke Model
            model.addAttribute("rankingResults", rankingResults);
            model.addAttribute("calculationSuccess", true); // Opsional: Indikator sukses

        } catch (Exception e) {
            // Tangani error jika perhitungan gagal
            model.addAttribute("rankingResults", List.of());
            model.addAttribute("calculationError", "Gagal menghitung ranking: " + e.getMessage());
            // Log error untuk debugging
            // logger.error("Ranking calculation failed", e);
        }

        return "calculation/ranking_result"; // Nama file Thymeleaf Anda
    }

    @GetMapping("/ranking/export")
    public void exportRankingResult(HttpServletResponse response) throws IOException {

        List<RankingResult> rankingResults = calculationService.calculateFinalRanking();

        setupResponse(response, "Final_Ranking_Result");
        try (XSSFWorkbook workbook = excelExportService.exportRankingResult(rankingResults)) {
            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/normalization/pdf")
    public void downloadNormalizationPdf(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Matriks_Normalisasi_Sub.pdf");
        calculationService.exportNormalizationPdf(response);
    }

    @GetMapping("/aggregated/pdf")
    public void downloadAggregatedPdf(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Matriks_Aggregated.pdf");
        calculationService.exportAggregatedPdf(response);
    }

    @GetMapping("/final-normalized/pdf")
    public void downloadFinalPdf(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Matriks_Final_Rij.pdf");
        calculationService.exportFinalNormalizedPdf(response);
    }

    @GetMapping("/ranking/pdf")
    public void downloadRankingPdf(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Laporan_Ranking_SAW.pdf");
        calculationService.exportRankingPdf(response);
    }

}