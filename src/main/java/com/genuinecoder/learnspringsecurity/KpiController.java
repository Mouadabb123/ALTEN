package com.genuinecoder.learnspringsecurity;

import com.genuinecoder.learnspringsecurity.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class KpiController {

    @Autowired
    private PlanDeTestRepository planDeTestRepository;

    @Autowired
    private EquipeRepository equipeRepository;



    @GetMapping("/testeurs")
    public String showKpisByTestLead(Model model) {
        List<PlanDeTest> allPlans = planDeTestRepository.findAll();
        Map<String, List<PlanDeTest>> plansByTestLead = new HashMap<>();

        // Regroup test plans by test lead
        for (PlanDeTest plan : allPlans) {
            for (MyUser testLead : plan.getTestLeads()) {
                String fullName = testLead.getNom() + " " + testLead.getPrenom();
                plansByTestLead.computeIfAbsent(fullName, k -> new ArrayList<>()).add(plan);
            }
        }

        List<Map<String, String>> kpis = new ArrayList<>();

        for (Map.Entry<String, List<PlanDeTest>> entry : plansByTestLead.entrySet()) {
            String leadName = entry.getKey();
            List<PlanDeTest> plans = entry.getValue();

            long total = plans.size();
            long ok = plans.stream().filter(p -> p.getStatus() == PlanDeTest.TestStatus.OK).count();
            long ko = plans.stream().filter(p -> p.getStatus() == PlanDeTest.TestStatus.KO).count();
            long executed = ok + ko;

            double couverture = total == 0 ? 0 : ((double) executed / total) * 100;
            double succes = executed == 0 ? 0 : ((double) ok / executed) * 100;
            double defauts = executed == 0 ? 0 : ((double) ko / executed) * 100;

            Map<String, String> row = new HashMap<>();
            row.put("lead", leadName);
            row.put("executionRate", String.format("%.2f", couverture));  // Taux de couverture
            row.put("successRate", String.format("%.2f", succes));        // Taux de réussite
            row.put("pendingRate", String.format("%.2f", defauts));       // Taux de défauts

            kpis.add(row);
        }

        model.addAttribute("kpis", kpis);
        return "kpi_cp";
    }




}