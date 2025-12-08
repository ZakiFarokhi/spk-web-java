package com.example.spk.dto;

import com.example.spk.entity.Pendidikan;
import com.example.spk.entity.Role;

public class AuditorDto {
    private String name;
    private String nip;
    private String jabatan;
    private String unit_kerja;
    private Long pendidikanId;
    private Pendidikan pendidikan;

    // getters & setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNip() { return nip; }
    public void setNip(String nip) { this.nip = nip; }

    public String getJabatan() { return jabatan; }
    public void setJabatan(String jabatan) { this.jabatan = jabatan; }

    public String getUnit_kerja() { return unit_kerja; }
    public void setUnit_kerja(String unit_kerja) { this.unit_kerja = unit_kerja; }

    public Long getPendidikanId() { return pendidikanId; }
    public void setPendidikanId(Long pendidikanId) { this.pendidikanId = pendidikanId; }

    public Pendidikan getPendidikan() { return pendidikan; }
    public void setPendidikan(Pendidikan pendidikan) { this.pendidikan = pendidikan; }
}
