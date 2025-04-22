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

import java.time.LocalDateTime;
import java.util.*;

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

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Seuls les Test Leaders peuvent modifier les plannings");
        }

        // Cloner les anciennes données pour les comparer
        PlanningDeTest oldPlanning = new PlanningDeTest();
        oldPlanning.setTache(planning.getTache());
        oldPlanning.setDateDebut(planning.getDateDebut());
        oldPlanning.setDateFin(planning.getDateFin());
        oldPlanning.setCommentaire(planning.getCommentaire());

        // Mise à jour des données
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



        historiqueService.enregistrerModification(oldPlanning, planning, currentUser);

        redirectAttributes.addFlashAttribute("success", "Planning de test mis à jour avec succès");
        return "redirect:/planning-de-test/liste";
    }
//    @GetMapping("/{id}/supprimer")
//    @Transactional
//    public void supprimerPlanningAvecHistorique(@PathVariable("id") Long id,
//                                                @RequestParam(value = "selectedLeaders", required = false) List<Long> selectedLeadersIds,
//                                                Authentication authentication,
//                                                MyUser utilisateurActuel) {
//        PlanningDeTest planning = planningDeTestRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Planning introuvable"));
//        System.out.println("ID reçu : " + id);
//
//        String email = authentication.getName();
//        MyUser currentUser = myUserRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
//
//        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
//            throw new AccessDeniedException("Seuls les Test Leaders peuvent modifier les plannings");
//        }
//        // Historisation AVANT suppression
//        HistoriquePlanning historique = new HistoriquePlanning();
//        historique.setPlanning(planning); // optionnel, car on va le détacher ensuite
//        historique.setUtilisateur(utilisateurActuel);
//        historique.setAction("SUPPRESSION");
//        historique.setDetails("Suppression du planning " + planning.getId()); // ou autre champ
//        historique.setAncienneValeur(planning.toString()); // ou JSON/fields importants
//        historique.setNouvelleValeur(null);
//        historique.setDateAction(LocalDateTime.now());
//        HistoriqueRepo.save(historique);
//
//        // Détacher le planning des historiques existants
//        List<HistoriquePlanning> historiques = HistoriqueRepo.findByPlanning(planning);
//        for (HistoriquePlanning h : historiques) {
//            h.setPlanning(null);
//        }
//        HistoriqueRepo.saveAll(historiques);
//
//        // Supprimer le planning
//        planningDeTestRepository.delete(planning);
//    }

    @GetMapping("/{id}/supprimer")
    public String deletePlanningDeTest(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        PlanningDeTest planning = planningDeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planning de test invalide: " + id));

        String email = authentication.getName();
        MyUser currentUser = myUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Vérifier que l'utilisateur est un TEST_LEADER
        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Seuls les Test Leaders peuvent supprimer les plannings");
        }

        try {
            // D'abord, supprimer tous les enregistrements d'historique associés
            List<HistoriquePlanning> historiques = HistoriqueRepo.findByPlanning(planning);
            if (historiques != null && !historiques.isEmpty()) {
                HistoriqueRepo.deleteAll(historiques);
            }

            // Ensuite, supprimer le planning
            planningDeTestRepository.delete(planning);

            redirectAttributes.addFlashAttribute("success", "Planning de test supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/planning-de-test/liste";
    }

    @GetMapping("/historique/{id}")
    public String afficherDetailsModification(@PathVariable Long id, Model model) {
        HistoriquePlanning historique = HistoriqueRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrée historique invalide: " + id));

        model.addAttribute("historique", historique);
        return "historique_details"; // Créez cette vue pour afficher tous les détails
    }


}