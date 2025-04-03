package com.genuinecoder.learnspringsecurity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user") // Compatible avec votre table existante
public class MyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // Remplace 'username'

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // Utilisation d'un Enum

    @Column(nullable = true) // Null pour CHEF_PROJET/TEST_LEADER
    private Integer disponibilite; // Jours avant disponibilité

    @Column(nullable = true) // Null pour CHEF_PROJET/TEST_LEADER
    private Integer experience; // Années d'expérience

    // Enum des rôles
    public enum Role {
        CHEF_PROJET, // Doit correspondre exactement à la valeur en base
        TEST_LEADER,
        TESTEUR
    }

    // Validation automatique avant sauvegarde
    @PrePersist
    @PreUpdate
    private void validate() {
        if (this.role == Role.TESTEUR) {
            if (this.disponibilite == null || this.experience == null) {
                throw new IllegalStateException("TESTEUR doit avoir disponibilite et experience renseignés");
            }
        } else {
            this.disponibilite = null;
            this.experience = null;
        }
    }

    // Getters/Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(Integer disponibilite) {
        this.disponibilite = disponibilite;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }
}