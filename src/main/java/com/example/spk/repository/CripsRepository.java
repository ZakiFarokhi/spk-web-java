package com.example.spk.repository;

import com.example.spk.entity.Crips;
import com.example.spk.entity.SubCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CripsRepository extends JpaRepository<Crips, Long> {

    List<Crips> findBySubCriteriaId(Long subCriteriaId);
    List<Crips> findBySubCriteria(SubCriteria subCriteria);
}
