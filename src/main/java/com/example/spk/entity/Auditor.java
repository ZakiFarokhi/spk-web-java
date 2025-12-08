package com.example.spk.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditors")
public class Auditor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name;

    @Column(nullable=false, unique = true)
    private String nip;

    @Column(nullable=false)
    private String jabatan;

    @Column(nullable=false)
    private String unit_kerja;

    @Column(nullable=false)
    private boolean enabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="pendidikan_id")
    private Pendidikan pendidikan;

    public Auditor(){}

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNip() { return nip; }
    public void setNip(String nip) { this.nip = nip; }

    public String getJabatan() { return jabatan; }
    public void setJabatan(String jabatan) { this.jabatan = jabatan; }

    public String getUnit_kerja() { return unit_kerja; }
    public void setUnit_kerja(String unit_kerja) { this.unit_kerja = unit_kerja; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Pendidikan getPendidikan() {return pendidikan;}
    public void setPendidikan(Pendidikan pendidikan){this.pendidikan = pendidikan;}
}
