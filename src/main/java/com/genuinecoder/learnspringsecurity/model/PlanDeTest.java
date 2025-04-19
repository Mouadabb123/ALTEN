package com.genuinecoder.learnspringsecurity.model;


import jakarta.persistence.*;
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
    private String Outils;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String Commentaire;

    public Set<MyUser> getTestLeads() {
        return testLeads;
    }

    public void setTestLeads(Set<MyUser> testLeads) {
        this.testLeads = testLeads;
    }

    @ManyToMany
    @JoinTable(
            name = "plan_test_lead",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "test_lead_id")
    )


    private Set<MyUser> testLeads;

    // Ajoutez cette nouvelle relation pour les testeurs
    @ManyToMany
    @JoinTable(
            name = "plan_testeurs",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "testeur_id")
    )
    private Set<MyUser> testeursAffectes = new HashSet<>();

    public PlanDeTest(Long id, String titre, String description, String outils, String commentaire) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        Outils = outils;
        Commentaire = commentaire;
    }

    public PlanDeTest() {}

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
        return Outils;
    }

    public void setOutils(String outils) {
        Outils = outils;
    }

    public String getCommentaire() {
        return Commentaire;
    }

    public void setCommentaire(String commentaire) {
        Commentaire = commentaire;
    }

    public Set<MyUser> getTesteursAffectes() {
        return testeursAffectes;
    }

    public void setTesteursAffectes(Set<MyUser> testeursAffectes) {
        this.testeursAffectes = testeursAffectes;
    }
    @Override
    public String toString() {
        return "PlanDeTest{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", Outils='" + Outils + '\'' +
                ", Commentaire='" + Commentaire + '\'' +
                '}';
    }
}
