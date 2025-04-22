package com.genuinecoder.learnspringsecurity.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoriquePlanDeTestRepository extends JpaRepository<HistoriquePlanDeTest, Long> {
    List<HistoriquePlanDeTest> findByPlanIdOrderByDateActionDesc(Long planId);
    List<HistoriquePlanDeTest> findByUtilisateurIdOrderByDateActionDesc(Long userId);
    List<HistoriquePlanDeTest> findAllByOrderByDateActionDesc();
    @Modifying
    @Query("DELETE FROM HistoriquePlanDeTest h WHERE h.plan.id = :planId")
    void deleteAllByPlanId(@Param("planId") Long planId);
}