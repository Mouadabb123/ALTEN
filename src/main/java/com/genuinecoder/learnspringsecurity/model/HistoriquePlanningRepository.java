package com.genuinecoder.learnspringsecurity.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoriquePlanningRepository extends JpaRepository<HistoriquePlanning, Long> {
    List<HistoriquePlanning> findByPlanningIdOrderByDateActionDesc(Long planningId);
    List<HistoriquePlanning> findByUtilisateurIdOrderByDateActionDesc(Long userId);

List<HistoriquePlanning> findByPlanning(PlanningDeTest planning);
}
