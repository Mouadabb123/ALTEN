package com.genuinecoder.learnspringsecurity.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriquePlanningRepository extends JpaRepository<HistoriquePlanning, Long> {
    List<HistoriquePlanning> findByPlanningIdOrderByDateActionDesc(Long planningId);
    List<HistoriquePlanning> findByUtilisateurIdOrderByDateActionDesc(Long userId);
}
