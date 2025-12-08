package com.example.spk.repository;


import com.example.spk.entity.Auditor;
import com.example.spk.entity.AlternativeScore;
import com.example.spk.entity.SubCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlternativeScoreRepository extends JpaRepository<AlternativeScore, Long> {

    Optional<AlternativeScore> findByAuditorAndSubCriteria(Auditor auditor, SubCriteria subCriteria);
    Optional<AlternativeScore> findByAuditorIdAndSubCriteriaId(Long auditorId, Long subCriteriaId);

    List<AlternativeScore> findByAuditor(Auditor auditor);

    List<AlternativeScore> findBySubCriteria(SubCriteria subCriteria);
}