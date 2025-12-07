package com.example.spk.repository;

import com.example.spk.entity.Criteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CriteriaRepository extends JpaRepository<Criteria, Long> {

    boolean existsByCode(String code);

    Optional<Criteria> findByCode(String code);

    Optional<Criteria> findByName(String name);
}
