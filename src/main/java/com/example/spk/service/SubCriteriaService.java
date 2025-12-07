package com.example.spk.service;

import com.example.spk.entity.Criteria;
import com.example.spk.entity.SubCriteria;
import com.example.spk.repository.SubCriteriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubCriteriaService {

    private final SubCriteriaRepository subCriteriaRepository;
    private final CriteriaService criteriaService;

    public SubCriteriaService(SubCriteriaRepository subCriteriaRepository,
                              CriteriaService criteriaService) {
        this.subCriteriaRepository = subCriteriaRepository;
        this.criteriaService = criteriaService;
    }

    public List<SubCriteria> findAll() {
        return subCriteriaRepository.findAll();
    }

    public List<SubCriteria> findByCriteria(Long criteriaId) {
        return subCriteriaRepository.findByCriteriaId(criteriaId);
    }

    public Optional<SubCriteria> findById(Long id) {
        return subCriteriaRepository.findById(id);
    }

    public SubCriteria save(SubCriteria subCriteria) {
        return subCriteriaRepository.save(subCriteria);
    }

    public void deleteById(Long id) {
        subCriteriaRepository.deleteById(id);
    }
}
