package com.example.spk.repository;

import com.example.spk.entity.AuditorScore;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditorScoreRepository extends JpaRepository<AuditorScore,Long> {

    long countByCriteria_Id(Long criteriaId);

    List<AuditorScore> findByCriteria_Id(Long criteriaId);

    // AuditorScoreRepository.java (Tambahkan method ini)
    @Modifying
    @Transactional
    void deleteByAuditor_Id(Long auditorId);

    // Untuk mengambil semua skor yang hanya terkait dengan ID SubCriteria tertentu
    List<AuditorScore> findBySubCriteria_Id(Long subCriteriaId);

    // Asumsi: Auditor dan SubCriteria juga menggunakan relasi objek
    AuditorScore findByAuditor_IdAndSubCriteria_Id(Long auditorId, Long subCriteriaId);
}
