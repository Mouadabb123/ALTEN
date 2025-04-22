package com.genuinecoder.learnspringsecurity.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_plan_de_test")
public class HistoriquePlanDeTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String ancienneValeur;

    @Column(columnDefinition = "TEXT")
    private String nouvelleValeur;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanDeTest plan;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private MyUser utilisateur;

    private String action; // "CREATION", "MODIFICATION", "SUPPRESSION"
    private String details;
    private LocalDateTime dateAction;

    // Constructeurs
    public HistoriquePlanDeTest() {
    }

    public HistoriquePlanDeTest(PlanDeTest plan, MyUser utilisateur, String action, String details, LocalDateTime dateAction) {
        this.plan = plan;
        this.utilisateur = utilisateur;
        this.action = action;
        this.details = details;
        this.dateAction = dateAction;
    }

    public HistoriquePlanDeTest(String ancienneValeur, String nouvelleValeur, PlanDeTest plan, MyUser utilisateur, String action, String details, LocalDateTime dateAction) {
        this.ancienneValeur = ancienneValeur;
        this.nouvelleValeur = nouvelleValeur;
        this.plan = plan;
        this.utilisateur = utilisateur;
        this.action = action;
        this.details = details;
        this.dateAction = dateAction;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAncienneValeur() {
        return ancienneValeur;
    }

    public void setAncienneValeur(String ancienneValeur) {
        this.ancienneValeur = ancienneValeur;
    }

    public String getNouvelleValeur() {
        return nouvelleValeur;
    }

    public void setNouvelleValeur(String nouvelleValeur) {
        this.nouvelleValeur = nouvelleValeur;
    }

    public PlanDeTest getPlan() {
        return plan;
    }

    public void setPlan(PlanDeTest plan) {
        this.plan = plan;
    }

    public MyUser getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(MyUser utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

    @Override
    public String toString() {
        return "HistoriquePlanDeTest{" +
                "id=" + id +
                ", plan=" + plan +
                ", utilisateur=" + utilisateur +
                ", action='" + action + '\'' +
                ", details='" + details + '\'' +
                ", dateAction=" + dateAction +
                '}';
    }
}