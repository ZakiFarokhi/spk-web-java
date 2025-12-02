package com.example.spk.service;

import com.example.spk.dto.AuditorDto;
import com.example.spk.dto.UserDto;
import com.example.spk.entity.Auditor;
import com.example.spk.entity.User;
import com.example.spk.repository.AuditorRepository;
import com.example.spk.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditorService {
    private final AuditorRepository auditorRepository;


    public AuditorService(AuditorRepository auditorRepository) {
        this.auditorRepository = auditorRepository;
    }

    public List<Auditor> findAll() { return auditorRepository.findAll(); }

    public Auditor findById(Long id) {
        return auditorRepository.findById(id).orElse(null);
    }

    public boolean namaExists(String nama) {
        return auditorRepository.existsByNama(nama);
    }

    public boolean jabatanExists(String jabatan) {
        return auditorRepository.existsByJabatan(jabatan);
    }

    public Auditor save(Auditor auditor) {
        return auditorRepository.save(auditor);
    }

    public Auditor create(AuditorDto dto) {
        Auditor auditor = new Auditor();
        auditor.setNama(dto.getNama());
        auditor.setNip(dto.getNip());
        auditor.setJabatan(dto.getJabatan());
        auditor.setUnit_kerja(dto.getUnit_kerja());
        auditor.setPendidikan(dto.getPendidikan());
        auditor.setEnabled(true);
        return save(auditor);
    }

    public void deleteById(Long id) {
        auditorRepository.deleteById(id);
    }

    public Auditor update(Long id, AuditorDto dto) {
        Auditor auditor = findById(id);
        if (auditor == null) return null;

        auditor.setNama(dto.getNama());
        auditor.setJabatan(dto.getJabatan());
        auditor.setUnit_kerja(dto.getUnit_kerja());
        auditor.setPendidikan(dto.getPendidikan());
        return save(auditor);
    }

}
