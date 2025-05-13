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
import java.util.logging.Logger;

@Controller
@RequestMapping("/testeur")
public class TesteurDashboardController {

    private static final Logger logger = Logger.getLogger(TesteurDashboardController.class.getName());

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
            @RequestParam("nouveauStatut") String nouveauStatutStr,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // Journalisation pour déboguer
            logger.info("Tentative de changement de statut: " + nouveauStatutStr + " pour le plan ID: " + id);

            PlanDeTest plan = planDeTestRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

            String email = authentication.getName();
            MyUser testeur = myUserRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

            // Vérifier que le testeur est bien affecté à ce plan
            if (!plan.getTesteursAffectes().contains(testeur)) {
                throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier ce plan");
            }

            // Convertir la chaîne en enum
            PlanDeTest.TestStatus nouveauStatut;
            try {
                nouveauStatut = PlanDeTest.TestStatus.valueOf(nouveauStatutStr);
                logger.info("Conversion en enum réussie: " + nouveauStatut);
            } catch (IllegalArgumentException e) {
                logger.severe("Erreur de conversion enum: " + e.getMessage());
                throw new IllegalArgumentException("Statut invalide: " + nouveauStatutStr, e);
            }

            // Afficher l'ancien statut pour vérification
            logger.info("Ancien statut: " + plan.getStatus());

            plan.setStatus(nouveauStatut);
            plan.setStatusUpdatedBy(testeur);
            plan.setStatusUpdateDate(LocalDateTime.now());

            PlanDeTest savedPlan = planDeTestRepository.save(plan);
            logger.info("Plan sauvegardé avec nouveau statut: " + savedPlan.getStatus());

            redirectAttributes.addFlashAttribute("success", "Statut mis à jour avec succès: " + nouveauStatut);
        } catch (IllegalArgumentException e) {
            logger.severe("Exception IllegalArgument: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Statut invalide: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Exception générale: " + e.getMessage());
            e.printStackTrace();  // Pour avoir la trace complète dans les logs
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du statut: " + e.getMessage());
        }

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