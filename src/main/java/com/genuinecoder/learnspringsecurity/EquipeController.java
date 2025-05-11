package com.genuinecoder.learnspringsecurity;

import com.genuinecoder.learnspringsecurity.model.Equipe;
import com.genuinecoder.learnspringsecurity.model.EquipeRepository;
import com.genuinecoder.learnspringsecurity.model.MyUser;
import com.genuinecoder.learnspringsecurity.model.MyUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/equipes")
public class EquipeController {

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private MyUserRepository userRepository;

    // Liste des équipes
    @GetMapping
    public String listEquipes(Model model) {
        model.addAttribute("equipes", equipeRepository.findAll());
        return "Equipe";
    }

    // Formulaire de création
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("equipe", new Equipe());
        model.addAttribute("chefs", userRepository.findByRole(MyUser.Role.CHEF_PROJET));
        model.addAttribute("testLeads", userRepository.findByRole(MyUser.Role.TEST_LEADER));
        return "listeEquipe";
    }

    // Création d'une équipe
    @PostMapping
    public String createEquipe(@ModelAttribute Equipe equipe,
                               @RequestParam Long chefId,
                               @RequestParam(required = false) Long testLeadId,
                               @RequestParam(required = false) List<Long> membresIds,
                               RedirectAttributes redirectAttributes) {

        MyUser chef = userRepository.findById(chefId)
                .orElseThrow(() -> new IllegalArgumentException("Chef invalide"));

        equipe.setChefDeProjet(chef);

        if (testLeadId != null) {
            MyUser testLead = userRepository.findById(testLeadId)
                    .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));
            equipe.setTestLead(testLead);
        }

        if (membresIds != null && !membresIds.isEmpty()) {
            Set<MyUser> membres = new HashSet<>(userRepository.findAllById(membresIds));
            equipe.setMembres(membres);
        }

        equipeRepository.save(equipe);
        redirectAttributes.addFlashAttribute("success", "Équipe créée");
        return "redirect:/equipes";
    }

    // Formulaire d'édition
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Équipe invalide"));

        model.addAttribute("equipe", equipe);
        model.addAttribute("chefs", userRepository.findByRole(MyUser.Role.CHEF_PROJET));
        model.addAttribute("testLeads", userRepository.findByRole(MyUser.Role.TEST_LEADER));
        model.addAttribute("membres", userRepository.findAll());

        return "equipes/form";
    }

    // Mise à jour
    @PostMapping("/{id}")
    public String updateEquipe(@PathVariable Long id,
                               @ModelAttribute Equipe equipe,
                               @RequestParam Long chefId,
                               @RequestParam(required = false) Long testLeadId,
                               @RequestParam(required = false) List<Long> membresIds,
                               RedirectAttributes redirectAttributes) {

        Equipe existing = equipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Équipe invalide"));

        existing.setNom(equipe.getNom());

        MyUser chef = userRepository.findById(chefId)
                .orElseThrow(() -> new IllegalArgumentException("Chef invalide"));
        existing.setChefDeProjet(chef);

        if (testLeadId != null) {
            MyUser testLead = userRepository.findById(testLeadId)
                    .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));
            existing.setTestLead(testLead);
        } else {
            existing.setTestLead(null);
        }

        if (membresIds != null && !membresIds.isEmpty()) {
            Set<MyUser> membres = new HashSet<>(userRepository.findAllById(membresIds));
            existing.setMembres(membres);
        } else {
            existing.setMembres(new HashSet<>());
        }

        equipeRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Équipe mise à jour");
        return "redirect:/equipes";
    }

    // Suppression
    @GetMapping("/{id}/delete")
    public String deleteEquipe(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        equipeRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Équipe supprimée");
        return "redirect:/equipes";
    }
}
