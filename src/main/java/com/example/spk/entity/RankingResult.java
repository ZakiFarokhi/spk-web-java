package com.example.spk.entity;

public class RankingResult {
    private Auditor auditor;
    private double finalScore;

    public RankingResult(Auditor auditor, double finalScore) {
        this.auditor = auditor;
        this.finalScore = finalScore;
    }

    // --- Getters and Setters ---
    public Auditor getAuditor() { return auditor; }
    public void setAuditor(Auditor auditor) { this.auditor = auditor; }
    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }
}