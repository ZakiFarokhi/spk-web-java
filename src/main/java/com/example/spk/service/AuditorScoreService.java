package com.example.spk.service;

import com.example.spk.entity.*;
import com.example.spk.repository.AuditorScoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuditorScoreService {

    // --- DEPENDENCIES ---
    @Autowired private AuditorScoreRepository auditorScoreRepository;
    @Autowired private AuditorService auditorService;
    @Autowired private CriteriaService criteriaService;
    @Autowired private SubCriteriaService subCriteriaService;
    @Autowired private CripsService cripsService;

    // --- CONSTANTS ---
    private static final Double DEFAULT_RAW_VALUE = 0.0;
    private static final Pattern RAW_VALUE_PATTERN = Pattern.compile("^rawValues\\[(\\d+)\\]$");


    // ====================================================================
    // 1. DATA RETRIEVAL & GENERATION
    // ====================================================================

    public List<AuditorScore> findAll(){
        return auditorScoreRepository.findAll();
    }

    public boolean isDataGenerated(Long criteriaId) {
        return auditorScoreRepository.countByCriteria_Id(criteriaId) > 0;
    }

    public List<AuditorScore> getScoresByCriteria(Long criteriaId) {
        return auditorScoreRepository.findByCriteria_Id(criteriaId);
    }

    @Transactional
    public Map<String, AuditorScore> convertListToMap(List<AuditorScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, AuditorScore> scoreMap = new HashMap<>();
        for (AuditorScore score : scores) {
            String key = score.getAuditor().getId() + "_" + score.getSubCriteria().getId();
            scoreMap.put(key, score);
        }
        return scoreMap;
    }

    @Transactional
    public void generateDefaultScores(Long criteriaId) {
        if (isDataGenerated(criteriaId)) {
            return;
        }

        List<Auditor> auditors = auditorService.findAll();
        List<SubCriteria> subCriteriaList = subCriteriaService.findByCriteriaId(criteriaId);
        Optional<Criteria> criteria = criteriaService.findById(criteriaId);

        if (!criteria.isPresent()) {
            throw new RuntimeException("Kriteria dengan ID " + criteriaId + " tidak ditemukan.");
        }

        for (Auditor auditor : auditors) {
            for (SubCriteria subCriteria : subCriteriaList) {
                AuditorScore score = new AuditorScore();

                score.setAuditor(auditor);
                score.setCriteria(criteria.get());
                score.setSubCriteria(subCriteria);
                score.setCrips(null);
                score.setRawValue(DEFAULT_RAW_VALUE);
                score.setNormalizedValue(null);

                auditorScoreRepository.save(score);
            }
        }
    }


    // ====================================================================
    // 2. DATA UPDATE & PICU NORMALISASI
    // ====================================================================

    /**
     * Memproses semua parameter dari form update per baris,
     * memfilter CSRF, dan menyimpan nilai mentah yang baru.
     */
    @Transactional
    public void updateScoresFromMap(Map<String, String> allParams) {
        Long lastCriteriaId = null;

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String rawValueStr = entry.getValue();

            Matcher matcher = RAW_VALUE_PATTERN.matcher(key);

            if (matcher.matches()) {
                Long scoreId = Long.parseLong(matcher.group(1));
                Double newRawValue;

                try {
                    newRawValue = Double.parseDouble(rawValueStr.replace(',', '.'));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Nilai tidak valid untuk skor ID " + scoreId + ": " + rawValueStr, e);
                }

                AuditorScore score = auditorScoreRepository.findById(scoreId)
                        .orElseThrow(() -> new RuntimeException("Skor ID " + scoreId + " tidak ditemukan."));

                // 1. Update Raw Value
                score.setRawValue(newRawValue);
                // 2. Clear Normalized Value (siap dihitung ulang)
                score.setNormalizedValue(null);
                // 3. Simpan ID Kriteria untuk pemicu normalisasi
                lastCriteriaId = score.getCriteria().getId();

                auditorScoreRepository.save(score);
            }
        }

        // Pemicu Perhitungan Normalisasi setelah semua update selesai
        if (lastCriteriaId != null) {
            this.recalculateNormalization(lastCriteriaId);
        }
    }


    // ====================================================================
    // 3. LOGIKA NORMALISASI
    // ====================================================================

    /**
     * Menghitung dan menyimpan nilai normalisasi untuk kriteria berjenis COST (Rumus: r = min / x).
     *
     */
    // AuditorScoreService.java

    /**
     * Menghitung dan menyimpan nilai normalisasi COST untuk setiap Sub-Kriteria di bawah criteriaId.
     * Rumus: r = min / x
     */
    private void calculateNormalizationCost(Long criteriaId) {
        // 1. Ambil semua Sub-Kriteria di bawah Kriteria C3
        List<SubCriteria> subCriteriaList = subCriteriaService.findByCriteriaId(criteriaId);

        if (subCriteriaList.isEmpty()) {
            System.out.println("Tidak ada Sub-Kriteria ditemukan untuk Kriteria ID: " + criteriaId);
            return;
        }

        // 2. Lakukan Normalisasi untuk SETIAP SUB-KRITERIA
        for (SubCriteria subCriteria : subCriteriaList) {
            Long subCriteriaId = subCriteria.getId();

            // Ambil semua skor auditor HANYA untuk Sub-Kriteria ini
            // ASUMSI: Anda memiliki method findBySubCriteria_Id di Repository
            List<AuditorScore> scoresForSubCriteria = auditorScoreRepository.findBySubCriteria_Id(subCriteriaId);

            if (scoresForSubCriteria.isEmpty()) continue;

            // 3. Hitung Nilai Minimum (min_i {x_ij}) untuk Sub-Kriteria ini
            double minValue = scoresForSubCriteria.stream()
                    .mapToDouble(AuditorScore::getRawValue)
                    .filter(v -> v > 0)
                    .min()
                    .orElse(1.0); // Default ke 1.0 jika semua data 0

            // 4. Iterasi dan Terapkan Rumus Cost PADA SKOR SUB-KRITERIA INI
            for (AuditorScore score : scoresForSubCriteria) {
                double rawValue = score.getRawValue();
                double normalizedValue;

                if (rawValue <= 0.0) {
                    normalizedValue = 0.0;
                } else {
                    normalizedValue = minValue / rawValue;
                }

                score.setNormalizedValue(normalizedValue);
                auditorScoreRepository.save(score);
            }
            System.out.println("Normalisasi Cost selesai untuk Sub-Kriteria: " + subCriteria.getCode());
        }
    }

    /**
     * Metode pembantu: Menyalin Raw Value ke Normalized Value.
     * Digunakan untuk kriteria BENEFIT.
     */
    @Transactional
    private void setRawValueAsNormalized(Long criteriaId) {
        List<AuditorScore> scores = auditorScoreRepository.findByCriteria_Id(criteriaId);
        for (AuditorScore score : scores) {
            System.out.println("Loaded 1");
            // Logika: Jika BENEFIT, normalizedValue = rawValue
            score.setNormalizedValue(score.getRawValue());
            auditorScoreRepository.save(score);
        }
    }

    // ====================================================================
    // 4. PICU NORMALISASI ULANG (Fleksibel)
    // ====================================================================

    /**
     * Pemicu utama untuk menghitung ulang normalisasi.
     * Memanggil perhitungan berdasarkan nilai 'indeks' (COST/BENEFIT) dari Entitas Criteria.
     */
    @Transactional
    public void recalculateNormalization(Long criteriaId) {
        Criteria criteria = criteriaService.findById(criteriaId)
                .orElseThrow(() -> new RuntimeException("Kriteria ID " + criteriaId + " tidak ditemukan untuk normalisasi."));

        String criteriaIndeks = criteria.getIndeks();
        System.out.println("INDEKS -"+criteriaId+" - "+ criteriaIndeks);

        if ("COST".equalsIgnoreCase(criteriaIndeks)) {
            calculateNormalizationCost(criteriaId);
            System.out.println("Normalisasi Cost selesai untuk kriteria: " + criteria.getCode() + ".");

        } else if ("BENEFIT".equalsIgnoreCase(criteriaIndeks)) {
            // Logika: Jika BENEFIT, rawValue disalin sebagai normalizedValue.
            setRawValueAsNormalized(criteriaId);
            System.out.println("Normalisasi Benefit selesai: Raw Value disalin untuk kriteria: " + criteria.getCode() + ".");

        } else {
            System.out.println("Tipe indeks kriteria tidak dikenal: " + criteriaIndeks);
        }
    }

    @Transactional
    public void updateScoresByCripsId(Map<Long, Long> scoresToUpdate) {

        for (Map.Entry<Long, Long> entry : scoresToUpdate.entrySet()) {
            Long scoreId = entry.getKey();      // ID dari AlternativeScore yang sudah ada
            Long newCripsId = entry.getValue(); // ID dari Crips yang baru dipilih

            // 1. Ambil AlternativeScore yang akan diupdate
            AuditorScore score = auditorScoreRepository.findById(scoreId)
                    .orElseThrow(() -> new IllegalArgumentException("AlternativeScore tidak ditemukan dengan ID: " + scoreId));

            // 2. Ambil objek Crips yang baru berdasarkan ID yang dipilih
            Crips newCrips = cripsService.findById(newCripsId)
                    .orElseThrow(() -> new IllegalArgumentException("Crips tidak ditemukan dengan ID: " + newCripsId));

            // 3. Update relasi Crips
            score.setCrips(newCrips);

            // 4. Update rawValue
            // Ini PENTING untuk sinkronisasi: rawValue harus sama dengan nilai Crips yang baru dipilih.
            score.setRawValue(newCrips.getNilai());

            // 5. Simpan perubahan (akan melakukan UPDATE)
            auditorScoreRepository.save(score);
        }
    }

}