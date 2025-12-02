package com.example.spk.repository;

import com.example.spk.entity.SubCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCriteriaRepository extends JpaRepository<SubCriteria, Long> {

    List<SubCriteria> findByCriteriaId(Long criteriaId);
}
