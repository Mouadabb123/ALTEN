package com.genuinecoder.learnspringsecurity.model;

import jakarta.persistence.*;

import java.util.Set;

import java.time.LocalDateTime;
@Entity
@Table(name = "Planning_De_Test")
public class PlanningDeTest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String tache;

    @Column(nullable = false)
    private String DateDebut;

    @Column(nullable = false)
    private String DateFin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String commentaire;

    @Enumerated(EnumType.STRING)
    private ValidationStatus validationStatus = ValidationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String validationComment;

    @ManyToOne
    private MyUser validatedBy;

    private LocalDateTime validationDate;

    public enum ValidationStatus {
        PENDING, APPROVED, REJECTED
    }

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(nullable = false)
    private Boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "equipe_id")
    private Equipe equipe;

    @ManyToOne
    @JoinColumn(name = "test_lead_id")
    private MyUser testLead;


    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    // Relation ManyToMany avec filtrage implicite des TEST_LEADER
    @ManyToMany
    @JoinTable(
            name = "planning_test_leads", // Table de jointure dédiée
            joinColumns = @JoinColumn(name = "planning_id"),
            inverseJoinColumns = @JoinColumn(name = "test_lead_id")
    )
    private Set<MyUser> testLeads; // Seuls les MyUser avec role=TEST_LEADER seront associés

    public PlanningDeTest(Long id, String tache, String dateDebut, String dateFin, String commentaire, LocalDateTime dateCreation, LocalDateTime dateModification, Set<MyUser> testLeads) {
        this.id = id;
        this.tache = tache;
        DateDebut = dateDebut;
        DateFin = dateFin;
        this.commentaire = commentaire;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.testLeads = testLeads;
    }

    public PlanningDeTest() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTache() {
        return tache;
    }

    public void setTache(String tache) {
        this.tache = tache;
    }

    public String getDateDebut() {
        return DateDebut;
    }

    public void setDateDebut(String dateDebut) {
        DateDebut = dateDebut;
    }

    public String getDateFin() {
        return DateFin;
    }

    public void setDateFin(String dateFin) {
        DateFin = dateFin;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Set<MyUser> getTestLeads() {
        return testLeads;
    }

    public void setTestLeads(Set<MyUser> testLeads) {
        this.testLeads = testLeads;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    public MyUser getTestLead() {
        return testLead;
    }

    public void setTestLead(MyUser testLead) {
        this.testLead = testLead;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getValidationComment() {
        return validationComment;
    }

    public void setValidationComment(String validationComment) {
        this.validationComment = validationComment;
    }

    public MyUser getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(MyUser validatedBy) {
        this.validatedBy = validatedBy;
    }

    public LocalDateTime getValidationDate() {
        return validationDate;
    }

    public void setValidationDate(LocalDateTime validationDate) {
        this.validationDate = validationDate;
    }

    @Override
    public String toString() {
        return "PlanningDeTest{" +
                "id=" + id +
                ", tache='" + tache + '\'' +
                ", DateDebut='" + DateDebut + '\'' +
                ", DateFin='" + DateFin + '\'' +
                ", commentaire='" + commentaire + '\'' +
                ", dateCreation=" + dateCreation +
                ", dateModification=" + dateModification +
                ", testLeads=" + testLeads +
           '}';
}
}
