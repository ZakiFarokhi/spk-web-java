package com.example.spk.repository;

import com.example.spk.entity.Pendidikan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendidikanRepository extends JpaRepository<Pendidikan, Long> {
    Optional<Pendidikan> findByName(String name);
}
