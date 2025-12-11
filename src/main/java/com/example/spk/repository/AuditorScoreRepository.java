package com.example.spk.repository;

import com.example.spk.entity.AuditorScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditorScoreRepository extends JpaRepository<AuditorScore,Long> {

    long countByCriteria_Id(Long criteriaId);

    List<AuditorScore> findByCriteria_Id(Long criteriaId);

    // Asumsi: Auditor dan SubCriteria juga menggunakan relasi objek
    AuditorScore findByAuditor_IdAndSubCriteria_Id(Long auditorId, Long subCriteriaId);
}
