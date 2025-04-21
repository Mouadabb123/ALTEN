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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/planning-de-test")
public class PlanningDeTestController {

    @Autowired
    private HistoriqueService historiqueService;

    @Autowired
    private HistoriquePlanningRepository HistoriqueRepo;

    @Autowired
    private PlanningDeTestRepository planningDeTestRepository;

    @Autowired
    private MyUserRepository myUserRepository;

    // Afficher le formulaire de création
    @GetMapping("/nouveau")
    public String showCreationForm(Model model, Authentication authentication) {
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Vérifier que l'utilisateur est un TEST_LEADER
        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Accès réservé aux Test Leaders");
        }

        List<PlanningDeTest> plannings = planningDeTestRepository.findAll();
        if (plannings == null) {
            plannings = new ArrayList<>();
        }
        model.addAttribute("plannings", plannings);

        List<MyUser> testLeaders = myUserRepository.findByRole(MyUser.Role.TEST_LEADER);
        testLeaders.remove(currentUser);
        model.addAttribute("testLeaders", testLeaders);

        return "home_test_leader_planning";
    }

    // Créer un nouveau planning de test
    @PostMapping("/creer")
    public String creerPlanningDeTest(
            @ModelAttribute PlanningDeTest planningDeTest,
            @RequestParam(value = "selectedLeaders", required = false) List<Long> selectedLeadersIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Seuls les Test Leaders peuvent créer des plannings de test");
        }

        Set<MyUser> testLeaders = new HashSet<>();
        testLeaders.add(currentUser);

        if (selectedLeadersIds != null && !selectedLeadersIds.isEmpty()) {
            List<MyUser> selectedLeaders = myUserRepository.findAllById(selectedLeadersIds);
            testLeaders.addAll(selectedLeaders);
        }

        planningDeTest.setTestLeads(testLeaders);
        planningDeTestRepository.save(planningDeTest);

        historiqueService.enregistrerAction(planningDeTest, currentUser, "CREATION",
                "Création du planning " + planningDeTest.getTache());

        redirectAttributes.addFlashAttribute("success", "Planning de test créé avec succès");
        return "redirect:/planning-de-test/liste";
    }

    // Lister tous les plannings de test
    @GetMapping("/liste")
    public String listerPlanningsDeTest(Model model, Authentication authentication) {
        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Accès réservé aux Test Leaders");
        }

        List<PlanningDeTest> plannings = planningDeTestRepository.findAll();
        model.addAttribute("plannings", plannings);

        return "liste_planning";
    }

    // Afficher un planning de test spécifique
    @GetMapping("/{id}")
    public String afficherPlanningDeTest(@PathVariable Long id, Model model, Authentication authentication) {
        PlanningDeTest planning = planningDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planning de test invalide: " + id));

        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Modifié: Autoriser l'accès à tous les Test Leaders
        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Accès réservé aux Test Leaders");
        }

        model.addAttribute("planning", planning);
        return "Details_planning";
    }

    // Afficher le formulaire d'édition
    @GetMapping("/{id}/modifier")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        PlanningDeTest planning = planningDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planning de test invalide: " + id));

        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Modifié: Autoriser la modification à tous les Test Leaders
        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Seuls les Test Leaders peuvent modifier les plannings");
        }

        model.addAttribute("planning", planning);

        List<MyUser> allTestLeaders = myUserRepository.findByRole(MyUser.Role.TEST_LEADER);
        model.addAttribute("testLeaders", allTestLeaders);

        return "modifier_planning";
    }

    // Mettre à jour un planning de test
    @PostMapping("/{id}/modifier")
    public String updatePlanningDeTest(
            @PathVariable Long id,
            @ModelAttribute PlanningDeTest planningDetails,
            @RequestParam(value = "selectedLeaders", required = false) List<Long> selectedLeadersIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        PlanningDeTest planning = planningDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planning de test invalide: " + id));

        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Modifié: Autoriser la mise à jour à tous les Test Leaders
        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Seuls les Test Leaders peuvent modifier les plannings");
        }

        planning.setTache(planningDetails.getTache());
        planning.setDateDebut(planningDetails.getDateDebut());
        planning.setDateFin(planningDetails.getDateFin());
        planning.setCommentaire(planningDetails.getCommentaire());

        if (selectedLeadersIds != null && !selectedLeadersIds.isEmpty()) {
            Set<MyUser> testLeaders = new HashSet<>();
            testLeaders.add(currentUser); // Toujours ajouter l'utilisateur courant

            List<MyUser> selectedLeaders = myUserRepository.findAllById(selectedLeadersIds);
            testLeaders.addAll(selectedLeaders);

            planning.setTestLeads(testLeaders);
        }

        planningDeTestRepository.save(planning);

        historiqueService.enregistrerAction(planning, currentUser, "MODIFICATION",
                "Modification du planning " + planning.getTache());

        redirectAttributes.addFlashAttribute("success", "Planning de test mis à jour avec succès");
        return "redirect:/planning-de-test/liste";
    }

    @GetMapping("/{id}/supprimer")
    public String deletePlanningDeTest(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        PlanningDeTest planning = planningDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planning de test invalide: " + id));

        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Modifié: Autoriser la suppression à tous les Test Leaders
        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Seuls les Test Leaders peuvent supprimer les plannings");
        }

        planningDeTestRepository.delete(planning);

        historiqueService.enregistrerAction(planning, currentUser, "SUPPRESSION",
                "Suppression du planning " + planning.getTache());

        redirectAttributes.addFlashAttribute("success", "Planning de test supprimé avec succès");
        return "redirect:/planning-de-test/liste";
    }

    @GetMapping("/historique/{id}")
    public String afficherDetailsModification(@PathVariable Long id, Model model) {
        HistoriquePlanning historique = HistoriqueRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrée historique invalide: " + id));
        PlanningDeTest planning = historique.getPlanning();
        model.addAttribute("planning", planning);
        model.addAttribute("historique", historique);
        return "Details_planning";
    }
}