package com.example.spk.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "criteria")
public class Criteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double bobot;

    @Column(nullable = false)
    private String indeks; // BENEFIT / COST

    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubCriteria> subCriteriaList = new ArrayList<>();


    // ====================
    // GETTER & SETTER
    // ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBobot() {
        return bobot;
    }

    public void setBobot(Double bobot) {
        this.bobot = bobot;
    }

    public String getIndeks() {
        return indeks;
    }

    public void setIndeks(String indeks) {
        this.indeks = indeks;
    }

    public List<SubCriteria> getSubCriteriaList() {
        return subCriteriaList;
    }

    public void setSubCriteriaList(List<SubCriteria> subCriteriaList) {
        this.subCriteriaList = subCriteriaList;
    }
}
