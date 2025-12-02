package com.example.spk.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "crips")
public class Crips {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double nilai;


    @ManyToOne
    @JoinColumn(name = "sub_criteria_id", nullable = false)
    private SubCriteria subCriteria;


    // ====================
    // GETTER & SETTER
    // ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getNilai() {
        return nilai;
    }

    public void setNilai(Double nilai) {
        this.nilai = nilai;
    }

    public SubCriteria getSubCriteria() {
        return subCriteria;
    }

    public void setSubCriteria(SubCriteria subCriteria) {
        this.subCriteria = subCriteria;
    }
}
