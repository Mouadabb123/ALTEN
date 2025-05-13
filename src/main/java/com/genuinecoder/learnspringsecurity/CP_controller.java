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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class CP_controller {

    @Autowired
    private MyUserRepository myUserRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private PlanningDeTestRepository planningDeTestRepository;

    // Dashboard Admin
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.CHEF_PROJET) {
            throw new AccessDeniedException("Accès réservé aux chefs de projet");
        }

        model.addAttribute("currentUser", currentUser);

        return "CP_Dashbard";
    }

    // Gestion des Test Leads
    @GetMapping("/test-leads")
    public String gestionTestLeads(Model model, Authentication authentication) {
        List<MyUser> testLeads = myUserRepository.findByRole(MyUser.Role.TEST_LEADER);
        model.addAttribute("testLeads", testLeads);
        return "liste_TestLead";
    }

    // Ajouter un Test Lead
    @PostMapping("/ajouter-test-lead")
    public String ajouterTestLead(@ModelAttribute MyUser testLead, RedirectAttributes redirectAttributes) {
        testLead.setRole(MyUser.Role.TEST_LEADER);

        myUserRepository.save(testLead);
        redirectAttributes.addFlashAttribute("success", "Test Lead ajouté avec succès");
        return "redirect:/admin/test-leads";
    }

    // Gestion des équipes
    @GetMapping("/equipes")
    public String gestionEquipes(Model model, Authentication authentication) {
        // 1. Récupérer l'utilisateur connecté
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // 2. Vérifier le rôle (si nécessaire)
        if (currentUser.getRole() != MyUser.Role.CHEF_PROJET) {
            throw new AccessDeniedException("Accès réservé aux chefs de projet");
        }

        // 3. Ajouter toutes les données nécessaires au modèle
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("testLeads", myUserRepository.findByRole(MyUser.Role.TEST_LEADER));
        // Remplacer allUsers par testeurs seulement
        model.addAttribute("testeurs", myUserRepository.findByRole(MyUser.Role.TESTEUR));
        model.addAttribute("equipes", equipeRepository.findByChefDeProjet(currentUser));

        return "listeEquipe";
    }

    // Ajouter une équipe
    @PostMapping("/ajouter-equipe")
    public String ajouterEquipe(@ModelAttribute Equipe equipe,
                                @RequestParam Long chefProjetId,
                                @RequestParam(required = false) Long testLeadId,
                                @RequestParam(required = false) List<Long> membresIds,
                                RedirectAttributes redirectAttributes) {

        MyUser chefProjet = myUserRepository.findById(chefProjetId)
                .orElseThrow(() -> new IllegalArgumentException("Chef de projet invalide"));

        equipe.setChefDeProjet(chefProjet);

        if (testLeadId != null) {
            MyUser testLead = myUserRepository.findById(testLeadId)
                    .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));
            equipe.setTestLead(testLead);
        }

        if (membresIds != null && !membresIds.isEmpty()) {
            Set<MyUser> membres = new HashSet<>(myUserRepository.findAllById(membresIds));
            equipe.setMembres(membres);
        }

        equipeRepository.save(equipe);
        redirectAttributes.addFlashAttribute("success", "Équipe créée avec succès");
        return "redirect:/admin/equipes";
    }

// Dans CP_controller.java

    @GetMapping("/equipes/{equipeId}/plannings")
    public String afficherPlanningsEquipe(@PathVariable Long equipeId, Model model, Authentication authentication) {
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.CHEF_PROJET) {
            throw new AccessDeniedException("Accès réservé aux chefs de projet");
        }

        // Récupérer l'équipe et vérifier qu'elle appartient bien au chef de projet
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new IllegalArgumentException("Équipe invalide"));

        if (!equipe.getChefDeProjet().equals(currentUser)) {
            throw new AccessDeniedException("Vous n'êtes pas le chef de projet de cette équipe");
        }

        // Récupérer les plannings associés à cette équipe
        List<PlanningDeTest> plannings = planningDeTestRepository.findByEquipe(equipe);

        model.addAttribute("equipe", equipe);
        model.addAttribute("plannings", plannings);
        model.addAttribute("currentUser", currentUser);

        return "planning_equipe";
    }
    // Modifier une équipe
    @PostMapping("/equipes/{id}/modifier")
    public String modifierEquipe(@PathVariable Long id,
                                 @RequestParam String nom,
                                 @RequestParam(required = false) Long testLeadId,
                                 @RequestParam(required = false) List<Long> membresIds,
                                 RedirectAttributes redirectAttributes) {

        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Équipe invalide"));

        equipe.setNom(nom);

        if (testLeadId != null) {
            MyUser testLead = myUserRepository.findById(testLeadId)
                    .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));
            equipe.setTestLead(testLead);
        } else {
            equipe.setTestLead(null);
        }

        if (membresIds != null) {
            Set<MyUser> membres = new HashSet<>(myUserRepository.findAllById(membresIds));
            equipe.setMembres(membres);
        } else {
            equipe.setMembres(new HashSet<>());
        }

        equipeRepository.save(equipe);
        redirectAttributes.addFlashAttribute("success", "Équipe modifiée avec succès");
        return "redirect:/admin/equipes";
    }


    // Mettre à jour une équipe
    @PostMapping("/equipes/update")
    public String updateEquipe(@ModelAttribute Equipe equipe,
                               @RequestParam Long chefProjetId,
                               @RequestParam(required = false) Long testLeadId,
                               @RequestParam(required = false) List<Long> membresIds,
                               RedirectAttributes redirectAttributes) {

        MyUser chefProjet = myUserRepository.findById(chefProjetId)
                .orElseThrow(() -> new IllegalArgumentException("Chef de projet invalide"));

        Equipe existingEquipe = equipeRepository.findById(equipe.getId())
                .orElseThrow(() -> new IllegalArgumentException("Équipe invalide"));

        existingEquipe.setNom(equipe.getNom());
        existingEquipe.setChefDeProjet(chefProjet);

        if (testLeadId != null) {
            MyUser testLead = myUserRepository.findById(testLeadId)
                    .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));
            existingEquipe.setTestLead(testLead);
        } else {
            existingEquipe.setTestLead(null);
        }

        if (membresIds != null && !membresIds.isEmpty()) {
            Set<MyUser> membres = new HashSet<>(myUserRepository.findAllById(membresIds));
            existingEquipe.setMembres(membres);
        } else {
            existingEquipe.setMembres(new HashSet<>());
        }

        equipeRepository.save(existingEquipe);
        redirectAttributes.addFlashAttribute("success", "Équipe mise à jour avec succès");
        return "redirect:/admin/equipes";
    }

    // Supprimer une équipe
    @PostMapping("/equipes/{id}/delete")
    public String deleteEquipe(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        equipeRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Équipe supprimée avec succès");
        return "redirect:/admin/equipes";
    }

    @GetMapping("/plannings-a-valider")
    public String planningsAValider(Model model, Authentication authentication) {
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.CHEF_PROJET) {
            throw new AccessDeniedException("Accès réservé aux chefs de projet");
        }

        // Récupérer les équipes du chef de projet
        List<Equipe> equipes = equipeRepository.findByChefDeProjet(currentUser);

        // Récupérer les plannings en attente de validation
        List<PlanningDeTest> plannings = planningDeTestRepository.findByEquipeInAndValidationStatus(
                equipes, PlanningDeTest.ValidationStatus.PENDING);

        model.addAttribute("plannings", plannings);
        return "plannings_a_valider";
    }

    @PostMapping("/valider-tous-plannings")
    public String validerTousPlannings(@RequestParam(required = false) String commentaire,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.CHEF_PROJET) {
            throw new AccessDeniedException("Accès réservé aux chefs de projet");
        }

        // Récupérer les équipes du chef de projet
        List<Equipe> equipes = equipeRepository.findByChefDeProjet(currentUser);

        // Récupérer tous les plannings en attente de cette équipe
        List<PlanningDeTest> plannings = planningDeTestRepository.findByEquipeInAndValidationStatus(
                equipes, PlanningDeTest.ValidationStatus.PENDING);

        // Valider tous les plannings
        for (PlanningDeTest planning : plannings) {
            planning.setValidationStatus(PlanningDeTest.ValidationStatus.APPROVED);
            planning.setValidationComment(commentaire);
            planning.setValidatedBy(currentUser);
            planning.setValidationDate(LocalDateTime.now());
        }

        planningDeTestRepository.saveAll(plannings);

        redirectAttributes.addFlashAttribute("success",
                "Tous les plannings (" + plannings.size() + ") ont été validés avec succès");
        return "redirect:/admin/equipes/" + equipes.get(0).getId() + "/plannings";
    }

    @PostMapping("/rejeter-tous-plannings")
    public String rejeterTousPlannings(@RequestParam String commentaire,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.CHEF_PROJET) {
            throw new AccessDeniedException("Accès réservé aux chefs de projet");
        }

        // Récupérer les équipes du chef de projet
        List<Equipe> equipes = equipeRepository.findByChefDeProjet(currentUser);

        // Récupérer tous les plannings en attente de cette équipe
        List<PlanningDeTest> plannings = planningDeTestRepository.findByEquipeInAndValidationStatus(
                equipes, PlanningDeTest.ValidationStatus.PENDING);

        // Rejeter tous les plannings
        for (PlanningDeTest planning : plannings) {
            planning.setValidationStatus(PlanningDeTest.ValidationStatus.REJECTED);
            planning.setValidationComment(commentaire);
            planning.setValidatedBy(currentUser);
            planning.setValidationDate(LocalDateTime.now());
        }

        planningDeTestRepository.saveAll(plannings);

        redirectAttributes.addFlashAttribute("warning",
                "Tous les plannings (" + plannings.size() + ") ont été rejetés");
        return "redirect:/admin/equipes/" + equipes.get(0).getId() + "/plannings";
    }



// Add these methods to your CP_controller class

    // Get the form for modifying a Test Lead
    @GetMapping("/test-leads/{id}/modifier")
    public String modifierTestLeadForm(@PathVariable Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.CHEF_PROJET) {
            throw new AccessDeniedException("Accès réservé aux chefs de projet");
        }

        MyUser testLead = myUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));

        // Verify this is actually a TEST_LEADER
        if (testLead.getRole() != MyUser.Role.TEST_LEADER) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un Test Lead");
        }

        model.addAttribute("testLead", testLead);
        model.addAttribute("currentUser", currentUser);

        return "modifier_test_lead";
    }

    // Process the modification of a Test Lead
    @PostMapping("/test-leads/{id}/modifier")
    public String modifierTestLead(@PathVariable Long id,
                                   @RequestParam String nom,
                                   @RequestParam String prenom,
                                   @RequestParam String email,
                                   @RequestParam(required = false) String password,
                                   RedirectAttributes redirectAttributes) {

        MyUser testLead = myUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));

        // Update the test lead information
        testLead.setNom(nom);
        testLead.setPrenom(prenom);
        testLead.setEmail(email);

        // Update password only if provided
        if (password != null && !password.isEmpty()) {
            testLead.setPassword(password);
            // In a real application, you should encrypt this password before saving
            // passwordEncoder.encode(password)
        }

        // Ensure role remains TEST_LEADER
        testLead.setRole(MyUser.Role.TEST_LEADER);



        myUserRepository.save(testLead);

        redirectAttributes.addFlashAttribute("success", "Test Lead modifié avec succès");
        return "redirect:/admin/test-leads";
    }

    @PostMapping("/test-leads/{id}/supprimer")
    public String supprimerTestLead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Vérifier si le Test Lead existe
            MyUser testLead = myUserRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));

            // Vérifier que c'est bien un Test Lead
            if (testLead.getRole() != MyUser.Role.TEST_LEADER) {
                throw new IllegalArgumentException("L'utilisateur n'est pas un Test Lead");
            }

            // Supprimer le Test Lead
            myUserRepository.delete(testLead);

            redirectAttributes.addFlashAttribute("success", "Test Lead supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression du Test Lead: " + e.getMessage());
        }

        return "redirect:/admin/test-leads";
    }
}