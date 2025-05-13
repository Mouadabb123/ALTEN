package com.genuinecoder.learnspringsecurity.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "plan_de_test")
public class PlanDeTest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String outils;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String commentaire;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private TestStatus status = TestStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    private Difficulte difficulte;

    public enum Difficulte {
        FACILE("Facile"),
        MOYEN("Moyen"),
        DIFFICILE("Difficile");

        private final String displayName;

        Difficulte(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
        @Override
        public String toString() {
            return this.name(); // Important pour la sérialisation/désérialisation
        }
        }
    @ManyToOne
    @JoinColumn(name = "status_updated_by_id")
    private MyUser statusUpdatedBy;

    @Column(name = "status_update_date")
    private LocalDateTime statusUpdateDate;



    public enum TestStatus {
        NOT_STARTED, ONGOING, OK, KO , BLOCKED
    }

    @ManyToMany
    @JoinTable(
            name = "plan_test_lead",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "test_lead_id")
    )
    private Set<MyUser> testLeads = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "plan_testeurs",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "testeur_id")
    )

    private Set<MyUser> testeursAffectes = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "equipe_id")
    private Equipe equipe;

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    // Constructors
    public PlanDeTest() {}

    public PlanDeTest(String titre, String description, String outils, String commentaire) {
        this.titre = titre;
        this.description = description;
        this.outils = outils;
        this.commentaire = commentaire;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOutils() {
        return outils;
    }

    public void setOutils(String outils) {
        this.outils = outils;
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

    public Set<MyUser> getTesteursAffectes() {
        return testeursAffectes;
    }

    public void setTesteursAffectes(Set<MyUser> testeursAffectes) {
        this.testeursAffectes = testeursAffectes;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
    }

    public MyUser getStatusUpdatedBy() {
        return statusUpdatedBy;
    }

    public void setStatusUpdatedBy(MyUser statusUpdatedBy) {
        this.statusUpdatedBy = statusUpdatedBy;
    }

    public LocalDateTime getStatusUpdateDate() {
        return statusUpdateDate;
    }

    public void setStatusUpdateDate(LocalDateTime statusUpdateDate) {
        this.statusUpdateDate = statusUpdateDate;
    }


    public Difficulte getDifficulte() {
        return difficulte;
    }

    public void setDifficulte(Difficulte difficulte) {
        this.difficulte = difficulte;
    }

    @Override
    public String toString() {
        return "PlanDeTest{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", outils='" + outils + '\'' +
                ", commentaire='" + commentaire + '\'' +
                ", status=" + status +
                ", difficulte=" + difficulte +
                ", statusUpdatedBy=" + statusUpdatedBy +
                ", statusUpdateDate=" + statusUpdateDate +
                ", testLeads=" + testLeads +
                ", testeursAffectes=" + testeursAffectes +
                '}';
    }
}