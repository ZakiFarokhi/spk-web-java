package com.example.spk.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "alternative_scores") // Nama tabel di database
public class AlternativeScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditor_id", nullable = false)
    private Auditor auditor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_criteria_id", nullable = false)
    private SubCriteria subCriteria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crips_id", nullable = false)
    private Crips crips;

    @Column(name = "raw_value", nullable = false, columnDefinition = "DECIMAL(5,2)")
    private Double rawValue;

    public AlternativeScore() {
    }

    public AlternativeScore(Auditor auditor, SubCriteria subCriteria, Crips crips, Double rawValue) {
        this.auditor = auditor;
        this.subCriteria = subCriteria;
        this.crips = crips;
        this.rawValue = rawValue;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Auditor getAuditor() {
        return auditor;
    }

    public void setAuditor(Auditor alternative) {
        this.auditor = auditor;
    }

    public SubCriteria getSubCriteria() {
        return subCriteria;
    }

    public void setSubCriteria(SubCriteria subCriteria) {
        this.subCriteria = subCriteria;
    }

    public Crips getCrips() {
        return crips;
    }

    public void setCrips(Crips crips) {
        this.crips = crips;
    }

    public Double getRawValue() {
        return rawValue;
    }

    public void setRawValue(Double rawValue) {
        this.rawValue = rawValue;
    }
}
