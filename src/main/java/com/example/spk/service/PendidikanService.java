package com.example.spk.service;

import com.example.spk.entity.Pendidikan;
import com.example.spk.entity.Role;
import com.example.spk.repository.PendidikanRepository;
import com.example.spk.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PendidikanService {
    private final PendidikanRepository pendidikanRepository;

    public PendidikanService(PendidikanRepository pendidikanRepository) {
        this.pendidikanRepository = pendidikanRepository;
    }

    public List<Pendidikan> findAll() { return pendidikanRepository.findAll(); }

    public Pendidikan findById(Long id) {
        return pendidikanRepository.findById(id).orElse(null);
    }

    public Pendidikan findByName(String name) {
        return pendidikanRepository.findByName(name).orElse(null);
    }

    public Pendidikan save(Pendidikan pendidikan) {
        return pendidikanRepository.save(pendidikan);
    }
}
