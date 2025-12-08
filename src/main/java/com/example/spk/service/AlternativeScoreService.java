package com.example.spk.service;


import com.example.spk.entity.Auditor;
import com.example.spk.entity.AlternativeScore;
import com.example.spk.entity.Crips;
import com.example.spk.entity.SubCriteria;
import com.example.spk.repository.AlternativeScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Transactional
    public AlternativeScore saveOrUpdateScore(Long auditorId, Long subCriteriaId, Long cripsId) {
        // 1. Ambil semua entitas yang diperlukan
        Auditor auditor = auditorService.findById(auditorId)
                .orElseThrow(() -> new RuntimeException("Alternative ID not found: " + auditorId));
        SubCriteria subCriteria = subCriteriaService.findById(subCriteriaId)
                .orElseThrow(() -> new RuntimeException("SubCriteria ID not found: " + subCriteriaId));
        Crips crips = cripsService.findById(cripsId)
                .orElseThrow(() -> new RuntimeException("Crips ID not found: " + cripsId));

        // Nilai mentah yang akan disimpan
        Double rawValue = crips.getNilai(); // Asumsi: Crips memiliki field getNilai() atau getValue()

        // 2. Cek apakah penilaian sudah ada (Matriks Keputusan Awal)
        Optional<AlternativeScore> existingScore = alternativeScoreRepository.findByAuditorAndSubCriteria(auditor, subCriteria);

        if (existingScore.isPresent()) {
            // 3. Jika sudah ada, lakukan UPDATE
            AlternativeScore scoreToUpdate = existingScore.get();
            scoreToUpdate.setCrips(crips);
            scoreToUpdate.setRawValue(rawValue);
            return alternativeScoreRepository.save(scoreToUpdate);
        } else {
            // 4. Jika belum ada, lakukan CREATE
            AlternativeScore newScore = new AlternativeScore(auditor, subCriteria, crips, rawValue);
            return alternativeScoreRepository.save(newScore);
        }
    }

    public List<AlternativeScore> findAllScores() {
        return alternativeScoreRepository.findAll();
    }

    public List<AlternativeScore> getScoresByAlternative(Long alternativeId) {
        Auditor alternative = auditorService.findById(alternativeId)
                .orElseThrow(() -> new RuntimeException("Alternative ID not found: " + alternativeId));
        return alternativeScoreRepository.findByAuditor(alternative);
    }
    @Transactional
    public void saveOrUpdateScore(Auditor auditor, SubCriteria subCriteria, Crips crips) {

        // 1. Cari skor yang sudah ada berdasarkan ID Auditor dan Sub-Kriteria
        Optional<AlternativeScore> existingScore = alternativeScoreRepository
                .findByAuditorIdAndSubCriteriaId(auditor.getId(), subCriteria.getId());

        AlternativeScore score;

        // 2 & 3. Dapatkan skor yang sudah ada ATAU buat objek baru jika tidak ada.
         score = existingScore.orElseGet(() -> {
            // Ini adalah Supplier (lambda function) yang hanya dieksekusi jika Optional kosong
            AlternativeScore newScore = new AlternativeScore();
            newScore.setAuditor(auditor);
            newScore.setSubCriteria(subCriteria);
            return newScore;
        });

        // 4. Set nilai baru (Crips dan rawValue)
        // rawValue diambil dari nilai numerik yang ada di entity Crips
        score.setCrips(crips);
        score.setRawValue(crips.getNilai()); // Asumsi field numerik Crips adalah 'nilai'

        // 5. Simpan (Spring Data JPA akan otomatis menentukan apakah ini INSERT atau UPDATE)
        alternativeScoreRepository.save(score);
    }

    public void deleteScore(Long id) {
        alternativeScoreRepository.deleteById(id);
    }
}