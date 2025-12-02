package com.example.spk.service;

import com.example.spk.entity.Crips;
import com.example.spk.entity.SubCriteria;
import com.example.spk.repository.CripsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CripsService {

    private final CripsRepository cripsRepository;
    private final SubCriteriaService subCriteriaService;

    public CripsService(CripsRepository cripsRepository,
                        SubCriteriaService subCriteriaService) {
        this.cripsRepository = cripsRepository;
        this.subCriteriaService = subCriteriaService;
    }

    public List<Crips> findAll() {
        return cripsRepository.findAll();
    }

    public List<Crips> findBySubCriteria(Long subCriteriaId) {
        return cripsRepository.findBySubCriteriaId(subCriteriaId);
    }

    public Optional<Crips> findById(Long id) {
        return cripsRepository.findById(id);
    }

    public Crips save(Long subCriteriaId, Crips crips) {
        SubCriteria sc = subCriteriaService.findById(subCriteriaId)
                .orElseThrow(() -> new RuntimeException("SubCriteria not found"));

        crips.setSubCriteria(sc);
        return cripsRepository.save(crips);
    }

    public void deleteById(Long id) {
        cripsRepository.deleteById(id);
    }
}
