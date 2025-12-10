package com.example.spk.repository;

import com.example.spk.entity.Criteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CriteriaRepository extends JpaRepository<Criteria, Long> {

    boolean existsByCode(String code);

    Optional<Criteria> findByCode(String code);

    Optional<Criteria> findByName(String name);

    @Query("SELECT DISTINCT c FROM Criteria c LEFT JOIN FETCH c.subCriteriaList")
    List<Criteria> findAllWithSubCriterias();

    @Query("SELECT c FROM Criteria c " +
            "LEFT JOIN FETCH c.subCriteriaList sc " +
            "LEFT JOIN FETCH sc.cripsList " +
            "WHERE c.id = :criteriaId")
    Optional<Criteria> findByIdWithDetails(@Param("criteriaId") Long criteriaId);
}
