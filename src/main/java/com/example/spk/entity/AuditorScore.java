package com.example.spk.entity;

import jakarta.persistence.*;

@Entity
@Table(name="auditor_score")
public class AuditorScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditor_id", nullable = false)
    private Auditor auditor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    private Criteria criteria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_criteria_id", nullable = false)
    private SubCriteria subCriteria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crips_id", nullable = true)
    private Crips crips;

    @Column(name = "raw_value", nullable = false, columnDefinition = "DECIMAL(5,2)")
    private Double rawValue;

    @Column(name = "normalized_value", columnDefinition = "DECIMAL(5,4)", nullable = true)
    private Double normalizedValue;

    public AuditorScore() {}

    public AuditorScore(Auditor auditor,Criteria criteria, SubCriteria subCriteria, Crips crips, Double rawValue,  Double normalizedValue) {
        this.auditor = auditor;
        this.subCriteria = subCriteria;
        this.crips = crips;
        this.rawValue = rawValue;
        this.criteria = criteria;
        this.normalizedValue = normalizedValue;
    }

    //Getter Setter
    public Long getId() { return id; }
    public void setId(Long id) {this.id = id;}

    public Auditor getAuditor() {return auditor;}
    public void setAuditor(Auditor auditor) {this.auditor = auditor;}

    public Criteria getCriteria() {return criteria;}
    public void setCriteria(Criteria criteria) {this.criteria = criteria;}

    public SubCriteria getSubCriteria() {return subCriteria;}
    public void setSubCriteria(SubCriteria subCriteria) {this.subCriteria = subCriteria;}

    public Crips getCrips() {return crips;}
    public void setCrips(Crips crips) {this.crips = crips;}

    public Double getRawValue() {return rawValue;}
    public void setRawValue(Double rawValue) {this.rawValue = rawValue;}

    public Double getNormalizedValue() {return normalizedValue;}
    public void setNormalizedValue(Double normalizedValue) {this.normalizedValue = normalizedValue;}
}
