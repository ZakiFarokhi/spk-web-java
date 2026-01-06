package com.example.spk.service;

import com.example.spk.dto.AuditorDto;
import com.example.spk.dto.UserDto;
import com.example.spk.entity.Auditor;
import com.example.spk.entity.SubCriteria;
import com.example.spk.entity.User;
import com.example.spk.repository.AuditorRepository;
import com.example.spk.repository.AuditorScoreRepository;
import com.example.spk.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.spk.service.AuditorService;

import java.util.List;
import java.util.Optional;

@Service
public class AuditorService {
    private final AuditorRepository auditorRepository;
    private final AuditorScoreRepository auditorScoreRepository;

    public AuditorService(AuditorRepository auditorRepository, AuditorScoreRepository auditorScoreRepository) {
        this.auditorRepository = auditorRepository;
        this.auditorScoreRepository = auditorScoreRepository;
    }

    public List<Auditor> findAll() { return auditorRepository.findAll(); }

    public Optional<Auditor> findById(Long id) {
        return auditorRepository.findById(id);
    }

    public boolean namaExists(String nama) {
        return auditorRepository.existsByName(nama);
    }

    public boolean jabatanExists(String jabatan) {
        return auditorRepository.existsByJabatan(jabatan);
    }

    public Auditor save(Auditor auditor) {
        return auditorRepository.save(auditor);
    }

    public Auditor create(AuditorDto dto) {
        Auditor auditor = new Auditor();
        auditor.setName(dto.getName());
        auditor.setNip(dto.getNip());
        auditor.setJabatan(dto.getJabatan());
        auditor.setUnit_kerja(dto.getUnit_kerja());
        auditor.setPendidikan(dto.getPendidikan());
        auditor.setEnabled(true);

        return save(auditor);
    }

    public void deleteById(Long id) {
        auditorScoreRepository.deleteByAuditor_Id(id);
        auditorRepository.deleteById(id);

    }

    public Auditor update(Long id, AuditorDto dto) {
        Optional<Auditor> a = auditorRepository.findById(id);
        Auditor auditor = a.get();
        if (auditor == null) return null;

        auditor.setName(dto.getName());
        auditor.setJabatan(dto.getJabatan());
        auditor.setUnit_kerja(dto.getUnit_kerja());
        auditor.setPendidikan(dto.getPendidikan());
        return save(auditor);
    }

}
