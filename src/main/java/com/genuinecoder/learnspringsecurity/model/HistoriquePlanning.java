package com.genuinecoder.learnspringsecurity.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_planning")
public class HistoriquePlanning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String ancienneValeur;

    @Column(columnDefinition = "TEXT")
    private String nouvelleValeur;

    @ManyToOne
    @JoinColumn(name = "planning_id", nullable = true) // important: nullable = true
    private PlanningDeTest planning;


    @Column(name = "planning_reference")
    private String planningReference;

    public String getPlanningReference() {
        return planningReference;
    }

    public void setPlanningReference(String planningReference) {
        this.planningReference = planningReference;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private MyUser utilisateur;


    private String action; // "CREATION", "MODIFICATION", "SUPPRESSION"
    private String details;
    private LocalDateTime dateAction;

    public HistoriquePlanning(Long id, PlanningDeTest planning, MyUser utilisateur, String action, String details, LocalDateTime dateAction) {
        this.id = id;
        this.planning = planning;
        this.utilisateur = utilisateur;
        this.action = action;
        this.details = details;
        this.dateAction = dateAction;
    }

    public HistoriquePlanning(Long id, String ancienneValeur, String nouvelleValeur, PlanningDeTest planning, MyUser utilisateur, String action, String details, LocalDateTime dateAction) {
        this.id = id;
        this.ancienneValeur = ancienneValeur;
        this.nouvelleValeur = nouvelleValeur;
        this.planning = planning;
        this.utilisateur = utilisateur;
        this.action = action;
        this.details = details;
        this.dateAction = dateAction;
    }

    public HistoriquePlanning() {

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlanningDeTest getPlanning() {
        return planning;
    }

    public void setPlanning(PlanningDeTest planning) {
        this.planning = planning;
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
        return "HistoriquePlanning{" +
                "id=" + id +
                ", planning=" + planning +
                ", utilisateur=" + utilisateur +
                ", action='" + action + '\'' +
                ", details='" + details + '\'' +
                ", dateAction=" + dateAction +
            '}';
}
}
