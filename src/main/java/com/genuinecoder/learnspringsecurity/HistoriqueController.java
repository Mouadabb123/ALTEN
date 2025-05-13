package com.genuinecoder.learnspringsecurity;

import com.genuinecoder.learnspringsecurity.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/historique")
public class HistoriqueController {

    @Autowired
    private HistoriquePlanningRepository historiqueRepo;

    // Add this new repository for test plan history
    @Autowired
    private HistoriquePlanDeTestRepository historiquePlanRepo;

    @Autowired
    private MyUserRepository userRepo;

    @GetMapping("/historique-plans")
    public String afficherHistoriquePlans(Model model) {
        // Fetch all history records including deletions using the correct repository
        List<HistoriquePlanDeTest> historiques = historiquePlanRepo.findAll();

        // Sort by date (most recent first)
        historiques.sort((h1, h2) -> h2.getDateAction().compareTo(h1.getDateAction()));

        model.addAttribute("historiques", historiques);
        return "historique_plan";
    }

    @GetMapping
    public String getHistorique(Authentication authentication, Model model) {
        String email = authentication.getName();
        MyUser currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        List<HistoriquePlanning> historique = historiqueRepo.findByUtilisateurIdOrderByDateActionDesc(currentUser.getId())
                .stream()
                .filter(h -> h.getAction() != null && (
                        h.getAction().equalsIgnoreCase("CREATION") ||
                                h.getAction().equalsIgnoreCase("MODIFICATION") ||
                                h.getAction().equalsIgnoreCase("SUPPRESSION")
                ))
                .toList(); // or .collect(Collectors.toList()) if you're using Java <16

        model.addAttribute("historique", historique);
        return "historique";
    }
}