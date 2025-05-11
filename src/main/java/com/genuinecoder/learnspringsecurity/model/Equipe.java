package com.genuinecoder.learnspringsecurity.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

    @Entity
    public class Equipe {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String nom;

        @ManyToOne
        @JoinColumn(name = "chef_id")
        private MyUser chefDeProjet;

        @ManyToOne
        @JoinColumn(name = "test_lead_id")
        private MyUser testLead;

        @ManyToMany
        @JoinTable(
                name = "equipe_membres",
                joinColumns = @JoinColumn(name = "equipe_id"),
                inverseJoinColumns = @JoinColumn(name = "user_id")
        )

        private Set<MyUser> membres = new HashSet<>();

    public Equipe(Long id, String nom, MyUser chefDeProjet, Set<MyUser> membres) {
        this.id = id;
        this.nom = nom;
        this.chefDeProjet = chefDeProjet;
        this.membres = membres;
    }


    public Equipe() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public MyUser getChefDeProjet() {
        return chefDeProjet;
    }

    public void setChefDeProjet(MyUser chefDeProjet) {
        this.chefDeProjet = chefDeProjet;
    }

    public Set<MyUser> getMembres() {
        return membres;
    }

    public void setMembres(Set<MyUser> membres) {
        this.membres = membres;
    }

        public MyUser getTestLead() {
            return testLead;
        }

        public void setTestLead(MyUser testLead) {
            this.testLead = testLead;
        }

    @Override
    public String toString() {
        return "Equipe{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", chefDeProjet=" + chefDeProjet +
                ", membres=" + membres +
                '}';
    }

        // Filtrer un TestLead parmi les membres de l'équipe
        public MyUser getTestLeadForEquipe(Equipe equipe) {
            for (MyUser membre : equipe.getMembres()) {
                if (membre.getRole() == MyUser.Role.TEST_LEADER) {
                    return membre;
                }
            }
            return null;  // Si aucun TestLead n'est trouvé
        }

    }
