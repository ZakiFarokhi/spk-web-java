package com.example.spk.service;

import com.example.spk.entity.Criteria;
import com.example.spk.entity.SubCriteria;
import com.example.spk.repository.CriteriaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CriteriaService {

    private final CriteriaRepository criteriaRepository;

    public CriteriaService(CriteriaRepository criteriaRepository) {
        this.criteriaRepository = criteriaRepository;
    }

    @Transactional()
    public List<Criteria> findAllWithSubCriterias() {
        return criteriaRepository.findAllWithSubCriterias();
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

    public Optional<Criteria> findCriteriaByIdWithDetails(Long criteriaId) {
        // Memanggil Query Method khusus yang menggunakan JOIN FETCH
        return criteriaRepository.findByIdWithDetails(criteriaId);
    }

    public Optional<Criteria> update(Long id, Criteria updatedCriteria) {
        Optional<Criteria> existingCriteria = criteriaRepository.findById(id);

        if (existingCriteria.isPresent()) {
            Criteria criteriaToUpdate = existingCriteria.get();

            criteriaToUpdate.setCode(updatedCriteria.getCode());
            criteriaToUpdate.setName(updatedCriteria.getName());
            criteriaToUpdate.setBobot(updatedCriteria.getBobot());
            criteriaToUpdate.setIndeks(updatedCriteria.getIndeks());

            // 3. Simpan perubahan (Karena ID ada, JPA melakukan UPDATE)
            Criteria savedEntity = criteriaRepository.save(criteriaToUpdate);
            return Optional.of(savedEntity);
        } else {
            return Optional.empty(); // Data tidak ditemukan
        }
    }

    public void deleteById(Long id) {
        criteriaRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return criteriaRepository.existsByCode(code);
    }
}
