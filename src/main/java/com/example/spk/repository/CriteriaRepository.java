package com.example.spk.repository;

import com.example.spk.entity.Criteria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriteriaRepository extends JpaRepository<Criteria, Long> {

    boolean existsByCode(String code);
}
