package com.example.spk.repository;

import com.example.spk.entity.Auditor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuditorRepository extends JpaRepository<Auditor, Long> {
    @EntityGraph(attributePaths = "pendidikan")
    Optional<Auditor> findByNama(String nama);
    boolean existsByNama(String username);
    boolean existsByJabatan(String jabatan);
}
