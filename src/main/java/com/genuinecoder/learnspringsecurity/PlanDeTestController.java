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

import java.util.ArrayList;
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
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));


        List<MyUser> testeurs = myUserRepository.findByRole(MyUser.Role.TESTEUR);
        List<PlanDeTest> plans = planDeTestRepository.findByTestLeadsContaining(currentUser);
        if (plans == null) {
            plans = new ArrayList<>(); // Ensure plans is never null
        }
        model.addAttribute("testeurs", testeurs); // Nouveau
        model.addAttribute("plans", plans);


        List<MyUser> testLeaders = myUserRepository.findByRole(MyUser.Role.TEST_LEADER);
        testLeaders.remove(currentUser);
        model.addAttribute("testLeaders", testLeaders);

        return "home_test_leader";
    }

    // Créer un nouveau plan de test
    @PostMapping("/creer")
    public String creerPlanDeTest(
            @ModelAttribute PlanDeTest planDeTest,
            @RequestParam(value = "selectedLeaders", required = false) List<Long> selectedLeadersIds,
            @RequestParam(value = "selectedTesteurs", required = false) List<Long> selectedTesteursIds, // Nouveau
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
        // Gestion des testeurs affectés (nouveau)
        if (selectedTesteursIds != null && !selectedTesteursIds.isEmpty()) {
            Set<MyUser> testeursAffectes = new HashSet<>(myUserRepository.findAllById(selectedTesteursIds));
            planDeTest.setTesteursAffectes(testeursAffectes);
        }

        planDeTest.setTestLeads(testLeaders);
        planDeTestRepository.save(planDeTest);

        redirectAttributes.addFlashAttribute("success", "Plan de test créé avec succès");
        return "redirect:/plan-de-test/liste";  // Changed redirect to /plan-de-test/liste
    }

    // Lister tous les plans de test d'un Test Leader
    @GetMapping("/liste")
    public String listerPlansDeTest(Model model, Authentication authentication) {
        String email = authentication.getName(); // ← pas de cast ici
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Accès réservé aux Test Leaders");
        }

        List<PlanDeTest> plans = planDeTestRepository.findByTestLeadsContaining(currentUser);
        model.addAttribute("plans", plans);

        return "liste_plans";
    }


    // Afficher un plan de test spécifique
    @GetMapping("/{id}")
    public String afficherPlanDeTest(@PathVariable Long id, Model model, Authentication authentication) {
        PlanDeTest plan = planDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

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

        String email = authentication.getName(); // ← Récupère l'email
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (!plan.getTestLeads().contains(currentUser)) {
            throw new AccessDeniedException("Vous n'avez pas le droit de modifier ce plan");
        }
        // Ajoutez la liste des testeurs disponibles
        List<MyUser> testeurs = myUserRepository.findByRole(MyUser.Role.TESTEUR);
        model.addAttribute("plan", plan);
        model.addAttribute("testeurs", testeurs);

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
            @RequestParam(value = "selectedTesteurs", required = false) List<Long> selectedTesteursIds, // Nouveau
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        PlanDeTest plan = planDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

        // ✅ Récupérer l'utilisateur connecté de manière sûre
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (!plan.getTestLeads().contains(currentUser)) {
            throw new AccessDeniedException("Vous n'avez pas le droit de modifier ce plan");
        }

        // ✅ Mettre à jour les champs
        plan.setTitre(planDetails.getTitre());
        plan.setDescription(planDetails.getDescription());
        plan.setOutils(planDetails.getOutils());
        plan.setCommentaire(planDetails.getCommentaire());

        // Mise à jour des testeurs affectés (nouveau)
        if (selectedTesteursIds != null) {
            Set<MyUser> testeursAffectes = new HashSet<>(myUserRepository.findAllById(selectedTesteursIds));
            plan.setTesteursAffectes(testeursAffectes);
        }

        // ✅ Mettre à jour les Test Leaders associés
        if (selectedLeadersIds != null && !selectedLeadersIds.isEmpty()) {
            Set<MyUser> testLeaders = new HashSet<>();
            testLeaders.add(currentUser);

            List<MyUser> selectedLeaders = myUserRepository.findAllById(selectedLeadersIds);
            testLeaders.addAll(selectedLeaders);

            plan.setTestLeads(testLeaders);
        }
        
        planDeTestRepository.save(plan);

        redirectAttributes.addFlashAttribute("success", "Plan de test mis à jour avec succès");
        return "redirect:/plan-de-test/liste";
    }



    @GetMapping("/{id}/supprimer")
    public String deletePlanDeTest(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        // Retrieve the plan by its ID
        PlanDeTest plan = planDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

        // Get the currently authenticated user
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Verify if the current user is one of the test leaders associated with the plan
        if (!plan.getTestLeads().contains(currentUser)) {
            throw new AccessDeniedException("Vous n'avez pas le droit de supprimer ce plan de test");
        }

        // Delete the plan from the database
        planDeTestRepository.delete(plan);

        // Add a flash attribute to show a success message
        redirectAttributes.addFlashAttribute("success", "Plan de test supprimé avec succès");

        // Redirect back to the list of plans
        return "redirect:/plan-de-test/liste";
    }

}
