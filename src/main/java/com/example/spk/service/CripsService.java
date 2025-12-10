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

    public Crips save(Crips crips) {

        return cripsRepository.save(crips);
    }

    public Optional<Crips> update(Long id, Crips updatedCrips) {
        Optional<Crips> existingCrips = cripsRepository.findById(id);

        if (existingCrips.isPresent()) {
            Crips cripsToUpdate = existingCrips.get();

            cripsToUpdate.setDescription(updatedCrips.getDescription());
            cripsToUpdate.setNilai(updatedCrips.getNilai());

            // 3. Simpan perubahan (Karena ID ada, JPA melakukan UPDATE)
            Crips savedEntity = cripsRepository.save(cripsToUpdate);
            return Optional.of(savedEntity);
        } else {
            return Optional.empty(); // Data tidak ditemukan
        }
    }

    public Crips update(Crips crips) {

        return cripsRepository.save(crips);
    }

    public void deleteById(Long id) {
        cripsRepository.deleteById(id);
    }

    public Crips findBestMatch(SubCriteria subCriteria, Double rawValue) {
        // 1. Ambil semua Crips untuk SubCriteria ini, diurutkan DESCENDING (dari Nilai tertinggi ke terendah)
        List<Crips> cripsList = cripsRepository.findBySubCriteriaOrderByNilaiDesc(subCriteria);

        if (cripsList.isEmpty()) {
            throw new IllegalStateException("Tidak ada data Crips yang didefinisikan untuk Sub Kriteria: " + subCriteria.getName());
        }

        // 2. Iterasi untuk mencari Crips yang memenuhi batas bawah
        for (Crips crips : cripsList) {
            // Jika Raw Value (input) lebih besar atau sama dengan Nilai Crips,
            // maka Crips ini adalah kualifikasi tertinggi yang dicapai.
            if (rawValue >= crips.getNilai()) {
                return crips;
            }
        }

        // 3. Jika Raw Value terlalu rendah (di bawah Nilai Crips terkecil)
        //    maka kembalikan Crips dengan Nilai terkecil/terendah (biasanya Crips terburuk).
        //    Karena diurutkan DESCENDING, Crips terkecil adalah elemen terakhir.
        return cripsList.get(cripsList.size() - 1);
    }
}
