package com.genuinecoder.learnspringsecurity.model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanningDeTestRepository extends JpaRepository<PlanningDeTest, Long> {
    List<PlanningDeTest> findByTestLeadsContaining(MyUser user);
    List<PlanningDeTest> findAll(); // New method to fetch all plans

    List<PlanningDeTest> findByEquipeIn(List<Equipe> equipes);
    List<PlanningDeTest> findByEquipe(Equipe equipe);

    List<PlanningDeTest> findByEquipeInAndValidationStatus(List<Equipe> equipes, PlanningDeTest.ValidationStatus status);
    List<PlanningDeTest> findByTestLeadsContains(MyUser testLead);
}