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
     */
    @Transactional(readOnly = true)
    public List<RankingResult> calculateFinalRanking() {
        //

        // 1. Ambil semua Auditor
        List<Auditor> auditors = auditorRepository.findAll();

        // 2. Ambil semua skor auditor yang sudah dinormalisasi
        List<AuditorScore> allScores = auditorScoreRepository.findAll();

        // 3. Kelompokkan skor berdasarkan Auditor
        Map<Auditor, List<AuditorScore>> scoresByAuditor = allScores.stream()
                .collect(Collectors.groupingBy(AuditorScore::getAuditor));

        // 4. Hitung Skor Preferensi (V) untuk setiap Auditor
        List<RankingResult> rankingResults = auditors.stream()
                .map(auditor -> {
                    List<AuditorScore> auditorScores = scoresByAuditor.getOrDefault(auditor, List.of());
                    double finalScore = 0.0;

                    for (AuditorScore score : auditorScores) {
                        // Pastikan normalizedValue dan bobot kriteria sudah ada
                        Double normalizedValue = score.getNormalizedValue() != null ? score.getNormalizedValue() : 0.0;

                        // Kriteria bobot harus diakses melalui entitas Criteria yang terkait dengan skor
                        Double weight = score.getCriteria() != null ? score.getCriteria().getBobot() : 0.0;

                        // Perhitungan Matriks V (V_ij = W_j * r_ij) dan Penjumlahan
                        finalScore += weight * normalizedValue;
                    }

                    return new RankingResult(auditor, finalScore);
                })
                .collect(Collectors.toList());

        // 5. Urutkan Ranking (Skor tertinggi adalah peringkat 1)
        rankingResults.sort(Comparator.comparingDouble(RankingResult::getFinalScore).reversed());

        return rankingResults;
    }

    // Matriks Pembobotan (V) = W_j * r_ij
    // Method ini bisa digunakan untuk menampilkan V_ij di Matriks Pembobotan
    @Transactional(readOnly = true)
    public Map<Long, Double> getWeightedScores() {
        List<AuditorScore> allScores = auditorScoreRepository.findAll();
        Map<Long, Double> weightedScoreMap = new HashMap<>(); // Key: AuditorScore.id

        for (AuditorScore score : allScores) {
            Double normalizedValue = score.getNormalizedValue() != null ? score.getNormalizedValue() : 0.0;
            Double weight = score.getCriteria() != null ? score.getCriteria().getBobot() : 0.0;

            double weightedValue = normalizedValue * weight;
            weightedScoreMap.put(score.getId(), weightedValue);
        }
        return weightedScoreMap;
    }


    @Transactional(readOnly = true)
    public Map<String, Double> calculateAggregatedNormalizationMatrix() {

        // 1. Ambil semua skor normalisasi
        List<AuditorScore> allScores = auditorScoreRepository.findAll();

        // 2. Kelompokkan skor berdasarkan Auditor dan Kriteria (untuk iterasi)
        // Key: Auditor, Value: Map<Criteria, List<AuditorScore>>
        Map<Auditor, Map<Criteria, List<AuditorScore>>> groupedScores = allScores.stream()
                .collect(Collectors.groupingBy(
                        AuditorScore::getAuditor,
                        Collectors.groupingBy(AuditorScore::getCriteria)
                ));

        Map<String, Double> aggregatedMap = new HashMap<>();

        // 3. Iterasi dan Aggregasi
        for (Map.Entry<Auditor, Map<Criteria, List<AuditorScore>>> entryAuditor : groupedScores.entrySet()) {
            Auditor auditor = entryAuditor.getKey();

            for (Map.Entry<Criteria, List<AuditorScore>> entryCriteria : entryAuditor.getValue().entrySet()) {
                Criteria criteria = entryCriteria.getKey();
                List<AuditorScore> subScores = entryCriteria.getValue();

                // Perhitungan Aggregasi
                double totalAggregatedScore = 0.0;

                // Asumsi Sederhana: Bobot Sub-Kriteria dianggap sama (Rata-rata sederhana)
                // Jika Sub-Kriteria memiliki bobot berbeda, ganti 1.0/subScores.size() dengan bobot Sub-Kriteria (W_sk)
                double subWeight = 1.0 / subScores.size();

                for (AuditorScore score : subScores) {
                    Double normalizedValue = score.getNormalizedValue() != null ? score.getNormalizedValue() : 0.0;

                    // Aggregasi: (Bobot Sub-Kriteria * Nilai Normalisasi Sub-Kriteria)
                    totalAggregatedScore += subWeight * normalizedValue;
                }

                // Simpan hasil aggregasi (Matriks R_c)
                String key = auditor.getId() + "_" + criteria.getId();
                aggregatedMap.put(key, totalAggregatedScore);
            }
        }

        return aggregatedMap;
    }

    @Transactional(readOnly = true)
    public Map<String, Double> calculateFinalNormalizedCriteriaMatrix() {

        // Ambil Matriks hasil aggregasi dari Step 2
        Map<String, Double> aggregatedMap = calculateAggregatedNormalizationMatrix();

        // 1. Kelompokkan berdasarkan Kriteria (untuk mencari Max per Kriteria)
        Map<Long, List<Double>> scoresByCriteriaId = new HashMap<>();

        for (Map.Entry<String, Double> entry : aggregatedMap.entrySet()) {
            String key = entry.getKey(); // Format: "AuditorId_CriteriaId"

            // Ekstraksi CriteriaId
            Long criteriaId = Long.parseLong(key.split("_")[1]);

            scoresByCriteriaId.computeIfAbsent(criteriaId, k -> new ArrayList<>()).add(entry.getValue());
        }

        // 2. Hitung Nilai Maksimum (Max) per Kriteria
        Map<Long, Double> maxPerCriteria = scoresByCriteriaId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().mapToDouble(Double::doubleValue).max().orElse(1.0)
                ));

        // 3. Normalisasi Akhir (r = x / max)
        Map<String, Double> finalNormalizedMap = new HashMap<>();

        for (Map.Entry<String, Double> entry : aggregatedMap.entrySet()) {
            String key = entry.getKey();
            Double aggregatedScore = entry.getValue();
            Long criteriaId = Long.parseLong(key.split("_")[1]);

            Double maxValue = maxPerCriteria.getOrDefault(criteriaId, 1.0);

            double finalScore = (maxValue > 0) ? (aggregatedScore / maxValue) : 0.0;

            finalNormalizedMap.put(key, finalScore);
        }

        return finalNormalizedMap;
    }
}