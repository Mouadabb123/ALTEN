package com.genuinecoder.learnspringsecurity;

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
@RequestMapping("/testeur")
public class TesteurController {

    @Autowired
    private MyUserRepository myUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/liste")
    public String listerTesteurs(Model model, Authentication authentication) {
        MyUser currentUser = myUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Accès réservé aux Test Leaders");
        }

        List<MyUser> testeurs = myUserRepository.findByRole(MyUser.Role.TESTEUR);
        model.addAttribute("testeurs", testeurs);
        return "liste_testeurs";
    }

    @PostMapping("/creer")
    public String creerTesteur(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam Boolean disponibilite,
            @RequestParam int experience,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        MyUser currentUser = myUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Seuls les Test Leaders peuvent créer des testeurs");
        }

        if (myUserRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Un utilisateur avec cet email existe déjà");
            return "redirect:/testeur/liste";
        }

        MyUser testeur = new MyUser();
        testeur.setNom(nom);
        testeur.setPrenom(prenom);
        testeur.setEmail(email);
        testeur.setPassword(passwordEncoder.encode(password));
        testeur.setDisponibilite(disponibilite);
        testeur.setExperience(experience);
        testeur.setRole(MyUser.Role.TESTEUR);

        myUserRepository.save(testeur);

        redirectAttributes.addFlashAttribute("success", "Testeur créé avec succès");
        return "redirect:/testeur/liste";
    }

    @GetMapping("/{id}/modifier")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        MyUser testeur = myUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Testeur invalide: " + id));

        MyUser currentUser = myUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Vous n'avez pas le droit de modifier ce testeur");
        }

        model.addAttribute("testeur", testeur);
        return "modifier_testeur";
    }

    @PostMapping("/{id}/modifier")
    public String updateTesteur(
            @PathVariable Long id,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam Boolean disponibilite,
            @RequestParam int experience,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        MyUser testeur = myUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Testeur invalide: " + id));

        MyUser currentUser = myUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Vous n'avez pas le droit de modifier ce testeur");
        }

        testeur.setNom(nom);
        testeur.setPrenom(prenom);
        testeur.setEmail(email);
        if (password != null && !password.isEmpty()) {
            testeur.setPassword(passwordEncoder.encode(password));
        }
        testeur.setDisponibilite(disponibilite);
        testeur.setExperience(experience);

        myUserRepository.save(testeur);

        redirectAttributes.addFlashAttribute("success", "Testeur mis à jour avec succès");
        return "redirect:/testeur/liste";
    }

    @GetMapping("/{id}/supprimer")
    public String deleteTesteur(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        MyUser testeur = myUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Testeur invalide: " + id));

        MyUser currentUser = myUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getRole() != MyUser.Role.TEST_LEADER) {
            throw new AccessDeniedException("Vous n'avez pas le droit de supprimer ce testeur");
        }

        myUserRepository.delete(testeur);

        redirectAttributes.addFlashAttribute("success", "Testeur supprimé avec succès");
        return "redirect:/testeur/liste";
    }
}