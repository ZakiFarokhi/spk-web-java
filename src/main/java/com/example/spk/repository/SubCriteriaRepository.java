package com.example.spk.repository;

import com.example.spk.entity.Criteria;
import com.example.spk.entity.SubCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubCriteriaRepository extends JpaRepository<SubCriteria, Long> {

    List<SubCriteria> findByCriteriaId(Long criteriaId);
    List<SubCriteria> findByCriteria(Criteria criteria);

    Optional<SubCriteria> findByCode(String code);

    Optional<SubCriteria> findByName(String name);
}
