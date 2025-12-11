package com.example.spk.service;

import com.example.spk.entity.*;
import com.example.spk.repository.AuditorRepository;
import com.example.spk.repository.AuditorScoreRepository;
import com.example.spk.repository.SubCriteriaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuditorScoreService {

    @Autowired
    private AuditorScoreRepository auditorScoreRepository;

    @Autowired
    private AuditorService auditorService;
    @Autowired
    private CriteriaService criteriaService;
    @Autowired
    private SubCriteriaService subCriteriaService;
    @Autowired
    private CripsService cripsService;

    private static final Double DEFAULT_RAW_VALUE = 0.0;

    public List<AuditorScore> findAll(){
        return auditorScoreRepository.findAll();
    }

    public boolean isDataGenerated(Long criteriaId) {
        return auditorScoreRepository.countByCriteria_Id(criteriaId) > 0;
    }

    public List<AuditorScore> getScoresByCriteria(Long criteriaId) {
        return auditorScoreRepository.findByCriteria_Id(criteriaId);
    }

    @Transactional // PENTING: Memastikan sesi Hibernate tetap terbuka saat mengakses relasi LAZY
    public Map<String, AuditorScore> convertListToMap(List<AuditorScore> scores) {
        Map<String, AuditorScore> scoreMap = new HashMap<>();
        for (AuditorScore score : scores) {
            // Akses relasi LAZY di sini. Sesi harus terbuka.
            String key = score.getAuditor().getId() + "_" + score.getSubCriteria().getId();
            scoreMap.put(key, score);
        }
        return scoreMap;
    }

    @Transactional
    public void generateDefaultScores(Long criteriaId) {
        if (isDataGenerated(criteriaId)) {
            // Jika sudah ada, hentikan proses
            return;
        }

        List<Auditor> auditors = auditorService.findAll();
        List<SubCriteria> subCriteriaList = subCriteriaService.findByCriteriaId(criteriaId);
        Optional<Criteria> criteria = criteriaService.findById(criteriaId);

        for (Auditor auditor : auditors) {
            for (SubCriteria subCriteria : subCriteriaList) {
                AuditorScore score = new AuditorScore();

                // --- Set Entitas dan Nilai Default ---
                score.setAuditor(auditor);
                score.setCriteria(criteria.get());
                score.setSubCriteria(subCriteria);
                score.setCrips(null);
                score.setRawValue(DEFAULT_RAW_VALUE);

                auditorScoreRepository.save(score);
            }
        }
    }

    @Transactional
    public void updateScore(Long scoreId, Long newCripsId) {
        AuditorScore existingScore = auditorScoreRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Skor tidak ditemukan."));

        // Cari Crips yang sebenarnya (ID > 0)
        Crips newCrips = cripsService.findById(newCripsId)
                .orElseThrow(() -> new RuntimeException("Crips baru tidak valid."));

        existingScore.setCrips(newCrips); // Set Crips (tidak lagi NULL)
        existingScore.setRawValue(newCrips.getNilai()); // Set Raw Value dari deskripsi Crips

        auditorScoreRepository.save(existingScore);
    }

}
