package com.genuinecoder.learnspringsecurity.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {
    List<Equipe> findByChefDeProjet(MyUser chef);
    List<Equipe> findByTestLead(MyUser testLead);

    Optional<Equipe> findOneByTestLead(MyUser testLead);
}

