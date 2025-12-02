package com.example.spk.service;

import com.example.spk.entity.Criteria;
import com.example.spk.repository.CriteriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CriteriaService {

    private final CriteriaRepository criteriaRepository;

    public CriteriaService(CriteriaRepository criteriaRepository) {
        this.criteriaRepository = criteriaRepository;
    }

    public List<Criteria> findAll() {
        return criteriaRepository.findAll();
    }

    public Optional<Criteria> findById(Long id) {
        return criteriaRepository.findById(id);
    }

    public Criteria save(Criteria criteria) {
        return criteriaRepository.save(criteria);
    }

    public void deleteById(Long id) {
        criteriaRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return criteriaRepository.existsByCode(code);
    }
}
