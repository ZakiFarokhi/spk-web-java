package com.example.spk.service;


import com.example.spk.entity.Auditor;
import com.example.spk.entity.AlternativeScore;
import com.example.spk.entity.Crips;
import com.example.spk.entity.SubCriteria;
import com.example.spk.repository.AlternativeScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AlternativeScoreService {

    @Autowired
    private AlternativeScoreRepository alternativeScoreRepository;

    // Asumsi: Anda memiliki service untuk mengambil entitas lain berdasarkan ID
    @Autowired
    private AuditorService auditorService;
    @Autowired
    private SubCriteriaService subCriteriaService;
    @Autowired
    private CripsService cripsService;

    public List<AlternativeScore> findAllScores() {
        return alternativeScoreRepository.findAll();
    }

    public List<AlternativeScore> getScoresByAlternative(Long alternativeId) {
        Auditor alternative = auditorService.findById(alternativeId)
                .orElseThrow(() -> new RuntimeException("Alternative ID not found: " + alternativeId));
        return alternativeScoreRepository.findByAuditor(alternative);
    }

    public void deleteScore(Long id) {
        alternativeScoreRepository.deleteById(id);
    }

    public Map<Long, Double> extractRawScores(Map<String, String> allParams) {
        Map<Long, Double> rawScores = new HashMap<>();

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Hanya proses parameter yang bernama 'raw_scores[ID_SUBKRITERIA]'
            if (key.startsWith("raw_scores[") && key.endsWith("]")) {
                try {
                    // Ekstraksi ID Sub Criteria
                    String idStr = key.substring(11, key.length() - 1);
                    Long subCriteriaId = Long.parseLong(idStr);

                    // Konversi nilai mentah (Raw Value) ke Double
                    Double rawValue = Double.parseDouble(value);
                    if (rawValue < 0) throw new NumberFormatException("Nilai tidak boleh negatif.");

                    rawScores.put(subCriteriaId, rawValue);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Nilai skor tidak valid atau format salah untuk kriteria: " + key, e);
                }
            }
        }
        if (rawScores.isEmpty()) {
            throw new IllegalArgumentException("Tidak ada nilai skor yang dikirimkan.");
        }
        return rawScores;
    }

    /**
     * Menyimpan atau memperbarui semua Raw Score untuk Auditor yang ditentukan.
     */
    @Transactional
    public void saveOrUpdateRawScores(Long auditorId, Map<Long, Double> rawScores) {
        // 1. Validasi keberadaan Auditor (Penyebab utama error not-null sebelumnya)
        Auditor auditor = auditorService.findById(auditorId)
                .orElseThrow(() -> new IllegalArgumentException("Auditor tidak ditemukan dengan ID: " + auditorId));

        for (Map.Entry<Long, Double> entry : rawScores.entrySet()) {
            Long subCriteriaId = entry.getKey();
            Double rawValue = entry.getValue();

            // 2. Validasi keberadaan SubCriteria
            SubCriteria sc = subCriteriaService.findById(subCriteriaId)
                    .orElseThrow(() -> new IllegalArgumentException("Sub Kriteria tidak ditemukan dengan ID: " + subCriteriaId));

            // 3. Tentukan Crips yang cocok
            Crips matchedCrips = cripsService.findBestMatch(sc, rawValue);

            System.out.println("LOADED TEST");
            System.out.println(matchedCrips.getDescription());
            System.out.println(matchedCrips.getSubCriteria().getId());
            System.out.println(auditorId);
            System.out.println(auditor.getName());
            System.out.println(auditor.getId());

            // 4. Cari skor yang sudah ada, atau buat objek baru
            Optional<AlternativeScore> existingScoreOpt = alternativeScoreRepository.findByAuditorAndSubCriteria(auditor, sc);
            AlternativeScore scoreToSave = existingScoreOpt.orElseGet(AlternativeScore::new);

            // 5. Set/Update data
            scoreToSave.setAuditor(auditor);
            scoreToSave.setSubCriteria(sc);
            scoreToSave.setRawValue(rawValue);
            scoreToSave.setCrips(matchedCrips);

            // 6. Simpan ke database (Akan menghasilkan INSERT atau UPDATE)
            alternativeScoreRepository.save(scoreToSave);
            // DEBUG: Jika Anda tidak melihat query, tambahkan System.out.println di sini untuk verifikasi.
        }
    }

    /**
     * Mencari Crips yang memiliki nilai (nilai) paling dekat dengan rawValue yang diinput.
     * Ini memastikan bahwa data yang disimpan selalu terkait dengan Crips yang valid.
     */
    private Crips findBestMatchingCrips(List<Crips> allCrips, Double rawValue) {
        // Gunakan fungsi min untuk mencari Crips dengan selisih absolut terkecil
        return allCrips.stream()
                .min(Comparator.comparingDouble(crips -> Math.abs(crips.getNilai() - rawValue)))
                .orElse(null);
    }

    public Map<String, AlternativeScore> findAllScoresAsMap() {
        List<AlternativeScore> scores = alternativeScoreRepository.findAll();
        Map<String, AlternativeScore> scoreMap = new HashMap<>( );

        for (AlternativeScore score : scores) {
            // Membuat kunci unik dari ID Auditor dan ID SubCriteria
            String key = score.getAuditor().getId() + "_" + score.getSubCriteria().getId();
            scoreMap.put(key, score);
        }
        return scoreMap;
    }

    public Map<String, AlternativeScore> findAllScoresAsMapForCriteriaAndAuditor(
            Long auditorId,
            List<SubCriteria> subCriteriaList) {

        Map<String, AlternativeScore> scoreMap = new HashMap<>();

        // 1. Muat objek Auditor (Penting untuk findByAuditorAndSubCriteria)
        Auditor auditor = auditorService.findById(auditorId)
                .orElseThrow(() -> new IllegalArgumentException("Auditor tidak ditemukan dengan ID: " + auditorId));

        // 2. Iterasi melalui Sub Criteria yang relevan dan cari skor yang sudah ada
        for (SubCriteria sc : subCriteriaList) {

            // Gunakan repository method untuk mencari skor berdasarkan pasangan Auditor dan SubCriteria
            Optional<AlternativeScore> existingScoreOpt =
                    alternativeScoreRepository.findByAuditorAndSubCriteria(auditor, sc);

            if (existingScoreOpt.isPresent()) {
                AlternativeScore score = existingScoreOpt.get();
                // Format key sesuai dengan kebutuhan Thymeleaf di form input
                String key = auditorId + "_" + sc.getId();
                scoreMap.put(key, score);
            }
        }

        return scoreMap;
    }
}