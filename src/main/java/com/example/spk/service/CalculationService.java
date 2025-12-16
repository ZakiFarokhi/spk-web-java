package com.example.spk.service;

import com.example.spk.entity.Auditor;
import com.example.spk.entity.AuditorScore;
import com.example.spk.entity.Criteria;
import com.example.spk.entity.RankingResult;
import com.example.spk.repository.AuditorRepository;
import com.example.spk.repository.AuditorScoreRepository;
import com.example.spk.repository.CriteriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CalculationService {

    @Autowired
    private AuditorRepository auditorRepository;
    @Autowired
    private AuditorScoreRepository auditorScoreRepository;
    @Autowired
    private CriteriaRepository criteriaRepository;

    // Asumsi: Semua skor sudah dinormalisasi oleh AuditorScoreService

    /**
     * Menghitung Matriks Pembobotan (V) dan Skor Akhir (Preferensi) untuk semua Auditor.
     * Rumus SAW: V_i = Σ (W_j * r_ij)
     * * @return List hasil perhitungan peringkat
     *
     */

    @Transactional(readOnly = true)
    public Map<String, Double> calculateAggregatedNormalizationMatrix() {

        // 1. Ambil semua skor
        List<AuditorScore> allScores = auditorScoreRepository.findAll();

        // 2. Kelompokkan skor berdasarkan Auditor dan Kriteria
        Map<Auditor, Map<Criteria, List<AuditorScore>>> groupedScores = allScores.stream()
                .collect(Collectors.groupingBy(
                        AuditorScore::getAuditor,
                        Collectors.groupingBy(AuditorScore::getCriteria)
                ));

        Map<String, Double> aggregatedMap = new HashMap<>();

        // 3. Iterasi dan Aggregasi (Hitung C_j,norm menggunakan normalized_value)
        for (Map.Entry<Auditor, Map<Criteria, List<AuditorScore>>> entryAuditor : groupedScores.entrySet()) {
            Auditor auditor = entryAuditor.getKey();

            for (Map.Entry<Criteria, List<AuditorScore>> entryCriteria : entryAuditor.getValue().entrySet()) {
                // Kita tidak perlu lagi membalik C3 di sini, karena diasumsikan
                // kolom normalized_value sudah mencerminkan nilai yang benar untuk C3.

                Criteria criteria = entryCriteria.getKey();
                List<AuditorScore> subScores = entryCriteria.getValue();

                double aggregatedScore = 0.0;

                for (AuditorScore score : subScores) {
                    // PENTING: Menggunakan nilai yang sudah dinormalisasi dari database
                    Double normalizedValue = score.getNormalizedValue() != null ? score.getNormalizedValue() : 0.0;

                    // Ambil Bobot Sub-Kriteria (W_sk)
                    Double subWeight = score.getSubCriteria() != null && score.getSubCriteria().getBobot() != null
                            ? score.getSubCriteria().getBobot()
                            : 0.0;

                    // Agregasi: C_j,norm += (Normalized Value * Sub-Weight)
                    aggregatedScore += normalizedValue * subWeight;
                }

                // Simpan hasil aggregasi (Matriks C_j,norm)
                // C_j,norm sekarang berada di rentang 0-1.
                String key = auditor.getId() + "_" + criteria.getId();
                aggregatedMap.put(key, aggregatedScore);
            }
        }

        return aggregatedMap; // Map berisi skor C_j,norm (rentang 0-1) per Auditor
    }

    @Transactional(readOnly = true)
    public Map<String, Double> calculateFinalNormalizedCriteriaMatrix() {

        // 1. Ambil Matriks hasil aggregasi dari Sub-Kriteria (C_j,norm)
        Map<String, Double> aggregatedMap = this.calculateAggregatedNormalizationMatrix();

        // 2. Kelompokkan berdasarkan Kriteria (untuk mencari Max per Kriteria)
        Map<Long, List<Double>> scoresByCriteriaId = new HashMap<>();

        // Inisialisasi Kriteria ID 3 (C3)
        final Long CRITERIA_ID_C3 = 3L;

        for (Map.Entry<String, Double> entry : aggregatedMap.entrySet()) {
            String key = entry.getKey(); // Format: "AuditorId_CriteriaId"

            String[] parts = key.split("_");
            if (parts.length < 2) continue;
            Long criteriaId = Long.parseLong(parts[1]);

            scoresByCriteriaId.computeIfAbsent(criteriaId, k -> new ArrayList<>()).add(entry.getValue());
        }

        // 3. Hitung Nilai Maksimum (Max) per Kriteria
        Map<Long, Double> maxPerCriteria = scoresByCriteriaId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().mapToDouble(Double::doubleValue).max().orElse(1.0)
                ));

        // Ambil Max Global untuk C3
        Double maxC3 = maxPerCriteria.getOrDefault(CRITERIA_ID_C3, 1.0);


        // 4. Normalisasi Pembagian Maksimum (r = x / max)
        Map<String, Double> finalNormalizedMap = new HashMap<>();

        for (Map.Entry<String, Double> entry : aggregatedMap.entrySet()) {
            String key = entry.getKey();
            Double aggregatedScore = entry.getValue(); // Skor C_j,norm

            String[] parts = key.split("_");
            if (parts.length < 2) continue;
            Long criteriaId = Long.parseLong(parts[1]);

            Double maxValue = maxPerCriteria.getOrDefault(criteriaId, 1.0);

            double finalScore = (maxValue > 0) ? (aggregatedScore / maxValue) : 0.0;

            finalNormalizedMap.put(key, finalScore);
        }

        return finalNormalizedMap;
    }

    @Transactional(readOnly = true)
    public List<RankingResult> calculateFinalRanking() {

        // 1. Ambil Auditor
        List<Auditor> auditors = auditorRepository.findAll();

        // 2. Mendapatkan Matriks Keputusan Ternormalisasi Akhir (R_ij)
        //    Input dari fungsi normalisasi kriteria sebelumnya.
        Map<String, Double> finalNormalizedMatrix = this.calculateFinalNormalizedCriteriaMatrix();

        // 3. Ambil Bobot Kriteria Utama (W_j)
        List<Criteria> criteriaList = criteriaRepository.findAll();
        Map<Long, Double> criteriaWeights = criteriaList.stream()
                .collect(Collectors.toMap(
                        Criteria::getId,
                        criteria -> criteria.getBobot() != null ? criteria.getBobot() : 0.0
                ));

        // 4. Hitung Skor Preferensi Akhir (Final Score) per Auditor
        List<RankingResult> rankingResults = auditors.stream()
                .map(auditor -> {
                    double finalScore = 0.0;

                    // Iterasi melalui semua Kriteria Utama
                    for (Criteria criteria : criteriaList) {

                        String key = auditor.getId() + "_" + criteria.getId();

                        // A. Ambil Matriks Ternormalisasi (R_ij)
                        Double normalizedScore = finalNormalizedMatrix.getOrDefault(key, 0.0);

                        // B. Ambil Bobot Kriteria Utama (W_j)
                        Double weight = criteriaWeights.getOrDefault(criteria.getId(), 0.0);

                        // C. Perkalian dan Agregasi
                        double weightedContribution = normalizedScore * weight;
                        finalScore += weightedContribution;

                    }


                    return new RankingResult(auditor, finalScore);
                })
                .collect(Collectors.toList());

        // 5. Urutkan Ranking (Skor tertinggi adalah peringkat 1)
        rankingResults.sort(Comparator.comparingDouble(RankingResult::getFinalScore).reversed());

        for (int i = 0; i < rankingResults.size(); i++) {
            RankingResult result = rankingResults.get(i);
        }

        return rankingResults;
    }
}