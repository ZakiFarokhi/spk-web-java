package com.example.spk.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pendidikan")
public class Pendidikan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name;

    public Pendidikan() {}
    public Pendidikan(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
