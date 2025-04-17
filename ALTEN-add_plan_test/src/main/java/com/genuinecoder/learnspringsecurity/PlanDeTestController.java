package com.genuinecoder.learnspringsecurity;

import com.genuinecoder.learnspringsecurity.model.MyUser;
import com.genuinecoder.learnspringsecurity.model.PlanDeTest;
import com.genuinecoder.learnspringsecurity.model.MyUserRepository;
import com.genuinecoder.learnspringsecurity.model.PlanDeTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/plan-de-test")
public class PlanDeTestController {

    @Autowired
    private PlanDeTestRepository planDeTestRepository;

    @Autowired
    private MyUserRepository myUserRepository;

    // Afficher le formulaire de création
    @GetMapping("/nouveau")
    public String showCreationForm(Model model, Authentication authentication) {
        MyUser currentUser = (MyUser) authentication.getPrincipal();

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Seuls les Test Leaders peuvent créer des plans de test");
        }
        List<PlanDeTest> plans = planDeTestRepository.findAll();
        model.addAttribute("plans", plans);

        // Récupérer tous les Test Leaders disponibles (sauf l'utilisateur courant)
        List<MyUser> testLeaders = myUserRepository.findByRole(MyUser.Role.TEST_LEADER);
        testLeaders.remove(currentUser); // Retirer l'utilisateur courant de la liste

        model.addAttribute("testLeaders", testLeaders);

        return "home_test_leade";
    }

    // Créer un nouveau plan de test
    @PostMapping("/creer")
    public String creerPlanDeTest(
            @ModelAttribute PlanDeTest planDeTest,
            @RequestParam(value = "selectedLeaders", required = false) List<Long> selectedLeadersIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName(); // Récupère l'email
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Seuls les Test Leaders peuvent créer des plans de test");
        }

        // Initialiser la collection des Test Leaders
        Set<MyUser> testLeaders = new HashSet<>();
        testLeaders.add(currentUser); // Ajouter l'utilisateur courant

        // Ajouter les autres Test Leaders sélectionnés
        if (selectedLeadersIds != null && !selectedLeadersIds.isEmpty()) {
            List<MyUser> selectedLeaders = myUserRepository.findAllById(selectedLeadersIds);
            testLeaders.addAll(selectedLeaders);
        }

        planDeTest.setTestLeads(testLeaders);
        planDeTestRepository.save(planDeTest);

        redirectAttributes.addFlashAttribute("success", "Plan de test créé avec succès");
        return "redirect:/test-leader/home";
    }

    // Lister tous les plans de test d'un Test Leader
    @GetMapping("/liste")
    public String listerPlansDeTest(Model model, Authentication authentication) {
        MyUser currentUser = (MyUser) authentication.getPrincipal();

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Accès réservé aux Test Leaders");
        }

        // Récupérer tous les plans où l'utilisateur est un des Test Leaders
        List<PlanDeTest> plans = planDeTestRepository.findByTestLeadsContaining(currentUser);
        model.addAttribute("plans", plans);

        return "liste_plans";
    }

    // Afficher un plan de test spécifique
    @GetMapping("/{id}")
    public String afficherPlanDeTest(@PathVariable Long id, Model model, Authentication authentication) {
        PlanDeTest plan = planDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

        MyUser currentUser = (MyUser) authentication.getPrincipal();

        // Vérifier que l'utilisateur a accès à ce plan
        if (!plan.getTestLeads().contains(currentUser)) {
            throw new AccessDeniedException("Vous n'avez pas accès à ce plan de test");
        }

        model.addAttribute("plan", plan);
        return "details_plan";
    }

    // Afficher le formulaire d'édition
    @GetMapping("/{id}/modifier")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        PlanDeTest plan = planDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

        MyUser currentUser = (MyUser) authentication.getPrincipal();

        if (!plan.getTestLeads().contains(currentUser)) {
            throw new AccessDeniedException("Vous n'avez pas le droit de modifier ce plan");
        }

        model.addAttribute("plan", plan);

        // Récupérer tous les Test Leaders disponibles
        List<MyUser> allTestLeaders = myUserRepository.findByRole(MyUser.Role.TEST_LEADER);
        model.addAttribute("testLeaders", allTestLeaders);

        return "modifier_plan";
    }

    // Mettre à jour un plan de test
    @PostMapping("/{id}/modifier")
    public String updatePlanDeTest(
            @PathVariable Long id,
            @ModelAttribute PlanDeTest planDetails,
            @RequestParam(value = "selectedLeaders", required = false) List<Long> selectedLeadersIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        PlanDeTest plan = planDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

        MyUser currentUser = (MyUser) authentication.getPrincipal();

        if (!plan.getTestLeads().contains(currentUser)) {
            throw new AccessDeniedException("Vous n'avez pas le droit de modifier ce plan");
        }

        // Mettre à jour les champs de base
        plan.setTitre(planDetails.getTitre());
        plan.setDescription(planDetails.getDescription());
        plan.setOutils(planDetails.getOutils());
        plan.setCommentaire(planDetails.getCommentaire());

        // Mettre à jour les Test Leaders associés si des sélections ont été faites
        if (selectedLeadersIds != null && !selectedLeadersIds.isEmpty()) {
            Set<MyUser> testLeaders = new HashSet<>();
            testLeaders.add(currentUser); // Garder l'utilisateur courant

            List<MyUser> selectedLeaders = myUserRepository.findAllById(selectedLeadersIds);
            testLeaders.addAll(selectedLeaders);

            plan.setTestLeads(testLeaders);
        }

        planDeTestRepository.save(plan);

        redirectAttributes.addFlashAttribute("success", "Plan de test mis à jour avec succès");
        return "redirect:/plan-de-test/" + id;
    }
}