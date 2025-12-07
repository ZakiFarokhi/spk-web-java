package com.example.spk.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sub_criteria")
public class SubCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String code;

    @Column(nullable = false)
    private String name;


    @ManyToOne
    @JoinColumn(name = "criteria_id", nullable = false)
    private Criteria criteria;


    @OneToMany(mappedBy = "subCriteria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Crips> cripsList = new ArrayList<>();


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

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    public List<Crips> getCripsList() {
        return cripsList;
    }

    public void setCripsList(List<Crips> cripsList) {
        this.cripsList = cripsList;
    }
}
