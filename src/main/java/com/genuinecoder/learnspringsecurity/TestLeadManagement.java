package com.genuinecoder.learnspringsecurity;


import org.springframework.security.crypto.password.PasswordEncoder;
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

import java.util.List;

@Controller
@RequestMapping("/chef-projet")
public class TestLeadManagement {

    @Autowired
    private MyUserRepository userRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    // Liste des Test Leads
    @GetMapping
    public String listTestLeads(Model model) {
        List<MyUser> testLeads = userRepository.findByRole(MyUser.Role.TEST_LEADER);
        model.addAttribute("testLeads", testLeads);
        return "TestLeadManagement";
    }

    // Formulaire de création
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("testLead", new MyUser());
        return "test-leads/form";
    }

    // Création d'un Test Lead
    @PostMapping
    public String createTestLead(@ModelAttribute MyUser testLead,
                                 @RequestParam String password,
                                 RedirectAttributes redirectAttributes) {
        testLead.setRole(MyUser.Role.TEST_LEADER);
        userRepository.save(testLead);
        redirectAttributes.addFlashAttribute("success", "Test Lead créé avec succès");
        return "redirect:/test-leads";
    }

    // Formulaire d'édition
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        MyUser testLead = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));
        model.addAttribute("testLead", testLead);
        return "test-leads/form";
    }

    // Mise à jour
    @PostMapping("/{id}")
    public String updateTestLead(@PathVariable Long id,
                                 @ModelAttribute MyUser testLead,
                                 @RequestParam(required = false) String password,
                                 RedirectAttributes redirectAttributes) {
        MyUser existing = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));

        existing.setNom(testLead.getNom());
        existing.setPrenom(testLead.getPrenom());
        existing.setEmail(testLead.getEmail());



        userRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Test Lead mis à jour");
        return "redirect:/test-leads";
    }

    // Suppression
    @GetMapping("/{id}/delete")
    public String deleteTestLead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        MyUser testLead = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));

        // Vérifier qu'il n'est pas affecté à des équipes
        if (!testLead.getEquipesDirigees().isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de supprimer : ce Test Lead est affecté à des équipes");
            return "redirect:/test-leads";
        }

        userRepository.delete(testLead);
        redirectAttributes.addFlashAttribute("success", "Test Lead supprimé");
        return "redirect:/test-leads";
    }

    // Affecter à une équipe
    @PostMapping("/{id}/assign")
    public String assignToTeam(@PathVariable Long id,
                               @RequestParam Long equipeId,
                               RedirectAttributes redirectAttributes) {
        MyUser testLead = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test Lead invalide"));

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new IllegalArgumentException("Équipe invalide"));

        equipe.setTestLead(testLead);
        equipeRepository.save(equipe);

        redirectAttributes.addFlashAttribute("success",
                "Test Lead affecté à l'équipe " + equipe.getNom());
        return "redirect:/test-leads";
    }
}
