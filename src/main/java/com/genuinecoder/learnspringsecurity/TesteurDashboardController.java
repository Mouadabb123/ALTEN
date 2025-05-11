package com.genuinecoder.learnspringsecurity;

import com.genuinecoder.learnspringsecurity.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/testeur")
public class TesteurDashboardController {

    @Autowired
    private PlanDeTestRepository planDeTestRepository;

    @Autowired
    private MyUserRepository myUserRepository;


    @Autowired
    private PlanningDeTestRepository planningDeTestRepository;




        @GetMapping("/mes-plans")
        public String afficherMesPlans(Model model, Authentication authentication) {
            String email = authentication.getName();
            MyUser testeur = myUserRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

            List<PlanDeTest> plansAssignes = planDeTestRepository.findByTesteursAffectesContaining(testeur);

            model.addAttribute("plans", plansAssignes);
            return "DashboardTesteur";
        }

        @PostMapping("/{id}/changer-statut")
        public String changerStatutPlan(
                @PathVariable Long id,
                @RequestParam("nouveauStatut") PlanDeTest.TestStatus nouveauStatut,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

            PlanDeTest plan = planDeTestRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

            String email = authentication.getName();
            MyUser testeur = myUserRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

            // Vérifier que le testeur est bien affecté à ce plan
            if (!plan.getTesteursAffectes().contains(testeur)) {
                throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier ce plan");
            }

            plan.setStatus(nouveauStatut);
            plan.setStatusUpdatedBy(testeur);
            plan.setStatusUpdateDate(LocalDateTime.now());
            planDeTestRepository.save(plan);

            redirectAttributes.addFlashAttribute("success", "Statut mis à jour avec succès");
            return "redirect:/testeur/mes-plans";

        }

    @GetMapping("/planning")
    public String planning(Model model, Authentication authentication) {
        MyUser testeur = myUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (testeur.getRole() != MyUser.Role.TESTEUR) {
            throw new AccessDeniedException("Accès réservé aux testeurs");
        }

        // Récupérer le planning du testeur
        List<PlanningDeTest> plannings = planningDeTestRepository.findAll(); // À adapter selon votre modèle
        model.addAttribute("plannings", plannings);

        return "PlanningTesteur";
    }
}