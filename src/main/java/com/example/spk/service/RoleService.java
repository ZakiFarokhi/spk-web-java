package com.example.spk.service;

import com.example.spk.entity.Role;
import com.example.spk.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() { return roleRepository.findAll(); }

    public Role findById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name).orElse(null);
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }
}
