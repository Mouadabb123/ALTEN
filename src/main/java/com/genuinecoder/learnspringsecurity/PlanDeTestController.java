package com.genuinecoder.learnspringsecurity;

import com.genuinecoder.learnspringsecurity.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/plan-de-test")
public class PlanDeTestController {

    @Autowired
    private PlanDeTestRepository planDeTestRepository;


    @Autowired
    private MyUserRepository myUserRepository;

    @Autowired
    private HistoriquePlanService historiqueService;


    @Autowired
    private HistoriquePlanDeTestRepository historiqueRepo;

    @GetMapping("/nouveau")
    public String showCreationForm(Model model, Authentication authentication) {
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        List<MyUser> testeurs = myUserRepository.findByRole(MyUser.Role.TESTEUR);
        List<MyUser> testLeaders = myUserRepository.findByRole(MyUser.Role.TEST_LEADER);
        testLeaders.remove(currentUser);

        model.addAttribute("testeurs", testeurs);
        model.addAttribute("testLeaders", testLeaders);
        model.addAttribute("plan", new PlanDeTest());
        model.addAttribute("plans", planDeTestRepository.findAll());

        return "redirect:/plan-de-test/liste";
    }

    @PostMapping("/creer")
    public String creerPlanDeTest(
            @ModelAttribute PlanDeTest planDeTest,
            @RequestParam(value = "selectedLeaders", required = false) List<Long> selectedLeadersIds,
            @RequestParam(value = "selectedTesteurs", required = false) List<Long> selectedTesteursIds,
            @RequestParam(value = "affectationAutomatique", required = false) Boolean affectationAutomatique,
            @RequestParam(value = "nombreTesteurs", required = false) Integer nombreTesteurs,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Seuls les Test Leaders peuvent créer des plans de test");
        }

        Set<MyUser> testLeaders = new HashSet<>();
        testLeaders.add(currentUser);
        if (selectedLeadersIds != null) {
            testLeaders.addAll(myUserRepository.findAllById(selectedLeadersIds));
        }

        Set<MyUser> testeursAffectes = new HashSet<>();

        // Logique d'affectation automatique
        if (Boolean.TRUE.equals(affectationAutomatique) && nombreTesteurs != null && planDeTest.getDifficulte() != null) {
            // Récupérer tous les testeurs disponibles avec le rôle TESTEUR
            List<MyUser> testeursDisponibles = myUserRepository.findByRoleAndDisponibilite(MyUser.Role.TESTEUR, true);

            // Vérifier si on a suffisamment de testeurs disponibles
            if (testeursDisponibles.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Aucun testeur disponible pour l'affectation automatique.");
                return "redirect:/plan-de-test/liste";
            }

            if (testeursDisponibles.size() < nombreTesteurs) {
                redirectAttributes.addFlashAttribute("error",
                        "Nombre de testeurs non disponible. Seulement " + testeursDisponibles.size() + " testeur(s) disponible(s).");
                return "redirect:/plan-de-test/liste";
            }

            // Trier les testeurs par expérience (du plus expérimenté au moins expérimenté)
            testeursDisponibles.sort((t1, t2) -> Integer.compare(t2.getExperience(), t1.getExperience()));

            // Sélectionner les meilleurs testeurs selon la difficulté
            List<MyUser> testeursSelectionnes;

            switch (planDeTest.getDifficulte()) {
                case DIFFICILE:
                    // Pour les tests difficiles, prendre les plus expérimentés
                    testeursSelectionnes = testeursDisponibles.subList(0, Math.min(nombreTesteurs, testeursDisponibles.size()));
                    break;
                case MOYEN:
                    // Pour les tests moyens, prendre ceux du milieu de la liste
                    int middleIndex = testeursDisponibles.size() / 2;
                    int startIndex = Math.max(0, middleIndex - (nombreTesteurs / 2));
                    int endIndex = Math.min(testeursDisponibles.size(), startIndex + nombreTesteurs);
                    testeursSelectionnes = testeursDisponibles.subList(startIndex, endIndex);
                    break;
                case FACILE:
                default:
                    // Pour les tests faciles, on peut prendre les moins expérimentés
                    int startIndexFacile = Math.max(0, testeursDisponibles.size() - nombreTesteurs);
                    testeursSelectionnes = testeursDisponibles.subList(startIndexFacile, testeursDisponibles.size());
            }

            // Ajouter les testeurs sélectionnés
            testeursAffectes.addAll(testeursSelectionnes);
        } else if (selectedTesteursIds != null) {
            // Affectation manuelle
            testeursAffectes.addAll(myUserRepository.findAllById(selectedTesteursIds));
        }

        planDeTest.setTestLeads(testLeaders);
        planDeTest.setTesteursAffectes(testeursAffectes);
        planDeTestRepository.save(planDeTest);

        historiqueService.enregistrerAction(planDeTest, currentUser, "CREATION",
                "Création du plan " + planDeTest.getTitre());

        redirectAttributes.addFlashAttribute("success", "Plan de test créé avec succès");
        return "redirect:/plan-de-test/liste";
    }

    @GetMapping("/liste")
    public String listerPlansDeTest(Model model, Authentication authentication) {
        MyUser currentUser = myUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Accès réservé aux Test Leaders");
        }

        model.addAttribute("plans", planDeTestRepository.findAll());
        model.addAttribute("testeurs", myUserRepository.findByRole(MyUser.Role.TESTEUR));
        model.addAttribute("difficulteValues", PlanDeTest.Difficulte.values());
        model.addAttribute("planDeTest", new PlanDeTest());
        return "liste_plans";
    }

    @GetMapping("/{id}")
    public String afficherPlanDeTest(@PathVariable Long id, Model model) {
        PlanDeTest plan = planDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));
        model.addAttribute("plan", plan);
        return "details_plan";
    }

    @GetMapping("/{id}/modifier")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        PlanDeTest plan = planDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

        MyUser currentUser = myUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Vous n'avez pas le droit de modifier ce plan");
        }

        model.addAttribute("plan", plan);
        model.addAttribute("testeurs", myUserRepository.findByRole(MyUser.Role.TESTEUR));
        model.addAttribute("testLeaders", myUserRepository.findByRole(MyUser.Role.TEST_LEADER));
        return "modifier_plan";
    }

    @PostMapping("/{id}/modifier")
    public String updatePlanDeTest(
            @PathVariable Long id,
            @ModelAttribute PlanDeTest planDetails,
            @RequestParam(value = "selectedLeaders", required = false) List<Long> selectedLeadersIds,
            @RequestParam(value = "selectedTesteurs", required = false) List<Long> selectedTesteursIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        PlanDeTest plan = planDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

        MyUser currentUser = myUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Vous n'avez pas le droit de modifier ce plan");
        }

        // Sauvegarder l'ancienne version pour l'historique
        PlanDeTest ancienPlan = new PlanDeTest();
        ancienPlan.setTitre(plan.getTitre());
        ancienPlan.setDescription(plan.getDescription());
        ancienPlan.setOutils(plan.getOutils());
        ancienPlan.setCommentaire(plan.getCommentaire());

        // Mettre à jour les champs de base
        plan.setTitre(planDetails.getTitre());
        plan.setDescription(planDetails.getDescription());
        plan.setOutils(planDetails.getOutils());
        plan.setCommentaire(planDetails.getCommentaire());

        // Mettre à jour les test leaders
        Set<MyUser> testLeaders = new HashSet<>();
        if (plan.getTestLeads() != null) {
            testLeaders.addAll(plan.getTestLeads());
        }
        testLeaders.add(currentUser);

        if (selectedLeadersIds != null) {
            testLeaders.addAll(myUserRepository.findAllById(selectedLeadersIds));
        }
        plan.setTestLeads(testLeaders);

        // Mettre à jour les testeurs affectés
        if (selectedTesteursIds != null) {
            plan.setTesteursAffectes(new HashSet<>(myUserRepository.findAllById(selectedTesteursIds)));
        } else {
            plan.setTesteursAffectes(new HashSet<>());
        }

        planDeTestRepository.save(plan);

        // Enregistrer la modification dans l'historique
        historiqueService.enregistrerModification(ancienPlan, plan, currentUser);

        redirectAttributes.addFlashAttribute("success", "Plan de test mis à jour avec succès");
        return "redirect:/plan-de-test/liste";
    }

    @GetMapping("/{id}/supprimer")
    @Transactional
    public String deletePlanDeTest(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        PlanDeTest plan = planDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

        MyUser currentUser = myUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Vous n'avez pas le droit de supprimer ce plan de test");
        }

        try {
            // Use specific method for deletion records
            historiqueService.enregistrerSuppression(plan, currentUser);

            // Then delete the plan
            planDeTestRepository.delete(plan);

            redirectAttributes.addFlashAttribute("success", "Plan de test supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/plan-de-test/liste";
    }

    @GetMapping("/historique/{id}")
    public String afficherDetailsModification(@PathVariable Long id, Model model) {
        HistoriquePlanDeTest historique = historiqueRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrée historique invalide: " + id));

        model.addAttribute("historique", historique);
        return "historique_plan_details";
    }
}