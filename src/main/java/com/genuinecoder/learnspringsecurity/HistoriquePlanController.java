package com.genuinecoder.learnspringsecurity;

import com.genuinecoder.learnspringsecurity.model.HistoriquePlanDeTest;
import com.genuinecoder.learnspringsecurity.model.HistoriquePlanDeTestRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HistoriquePlanController {

    private final HistoriquePlanDeTestRepository historiqueRepo;

    public HistoriquePlanController(HistoriquePlanDeTestRepository historiqueRepo) {
        this.historiqueRepo = historiqueRepo;
    }

    @GetMapping("/historique-plan")
    public String afficherHistorique(Model model) {
        List<HistoriquePlanDeTest> historique = historiqueRepo.findAllByOrderByDateActionDesc();
        model.addAttribute("historique", historique);
        return "historique_plan";
    }
}