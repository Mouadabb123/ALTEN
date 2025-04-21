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

import java.util.*;

@Controller
@RequestMapping("/plan-de-test")
public class PlanDeTestController {

    @Autowired
    private PlanDeTestRepository planDeTestRepository;

    @Autowired
    private MyUserRepository myUserRepository;

    @GetMapping("/nouveau")
    public String showCreationForm(Model model, Authentication authentication) {
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Get all available testers and test leaders
        List<MyUser> testeurs = myUserRepository.findByRole(MyUser.Role.TESTEUR);
        List<MyUser> testLeaders = myUserRepository.findByRole(MyUser.Role.TEST_LEADER);
        testLeaders.remove(currentUser);

        model.addAttribute("testeurs", testeurs);
        model.addAttribute("testLeaders", testLeaders);
        model.addAttribute("plan", new PlanDeTest());
        model.addAttribute("plans", planDeTestRepository.findAll());

        return "home_test_leader";
    }

    @PostMapping("/creer")
    public String creerPlanDeTest(
            @ModelAttribute PlanDeTest planDeTest,
            @RequestParam(value = "selectedLeaders", required = false) List<Long> selectedLeadersIds,
            @RequestParam(value = "selectedTesteurs", required = false) List<Long> selectedTesteursIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Seuls les Test Leaders peuvent créer des plans de test");
        }

        // Set test leads (current user + selected leaders)
        Set<MyUser> testLeaders = new HashSet<>();
        testLeaders.add(currentUser);
        if (selectedLeadersIds != null) {
            testLeaders.addAll(myUserRepository.findAllById(selectedLeadersIds));
        }

        // Set assigned testers
        Set<MyUser> testeursAffectes = new HashSet<>();
        if (selectedTesteursIds != null) {
            testeursAffectes.addAll(myUserRepository.findAllById(selectedTesteursIds));
        }

        planDeTest.setTestLeads(testLeaders);
        planDeTest.setTesteursAffectes(testeursAffectes);
        planDeTestRepository.save(planDeTest);

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

        if (!plan.getTestLeads().contains(currentUser)) {
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

        if (!plan.getTestLeads().contains(currentUser)) {
            throw new AccessDeniedException("Vous n'avez pas le droit de modifier ce plan");
        }

        // Update basic fields
        plan.setTitre(planDetails.getTitre());
        plan.setDescription(planDetails.getDescription());
        plan.setOutils(planDetails.getOutils());
        plan.setCommentaire(planDetails.getCommentaire());

        // Update test leaders
        Set<MyUser> testLeaders = new HashSet<>();
        testLeaders.add(currentUser);
        if (selectedLeadersIds != null) {
            testLeaders.addAll(myUserRepository.findAllById(selectedLeadersIds));
        }
        plan.setTestLeads(testLeaders);

        // Update assigned testers
        if (selectedTesteursIds != null) {
            plan.setTesteursAffectes(new HashSet<>(myUserRepository.findAllById(selectedTesteursIds)));
        } else {
            plan.setTesteursAffectes(new HashSet<>());
        }

        planDeTestRepository.save(plan);
        redirectAttributes.addFlashAttribute("success", "Plan de test mis à jour avec succès");
        return "redirect:/plan-de-test/liste";
    }

    @GetMapping("/{id}/supprimer")
    public String deletePlanDeTest(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        PlanDeTest plan = planDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan de test invalide: " + id));

        MyUser currentUser = myUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (!plan.getTestLeads().contains(currentUser)) {
            throw new AccessDeniedException("Vous n'avez pas le droit de supprimer ce plan de test");
        }

        planDeTestRepository.delete(plan);
        redirectAttributes.addFlashAttribute("success", "Plan de test supprimé avec succès");
        return "redirect:/plan-de-test/liste";
    }
}