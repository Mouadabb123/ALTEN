package com.genuinecoder.learnspringsecurity;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class ContentController {

  @GetMapping("/home")
  public String handleWelcome(Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      for (GrantedAuthority authority : authentication.getAuthorities()) {
        switch (authority.getAuthority()) {
          case "ROLE_CHEF_PROJET":
            return "redirect:/chef-projet/home";
          case "ROLE_TEST_LEADER":
            return "redirect:/test-leader/home";
          case "ROLE_TESTEUR":
            return "redirect:/testeur/home";
        }
      }
    }
    return "home";
  }

  @GetMapping("/chef-projet/home")
  public String handleChefProjetHome() {
    return "home_chef_projet";
  }

  @GetMapping("/test-leader/home")
  public String handleTestLeaderHome() {
    return "home_test_leader";
  }

  @GetMapping("/testeur/home")
  public String handleTesteurHome() {
    return "home_testeur";
  }


    @GetMapping("/login")
    public String showLoginPage(
            HttpServletRequest request,
            Model model) {

      // Récupère le paramètre d'erreur de la session
      Object error = request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");

      if (error != null) {
        model.addAttribute("error", "Email ou mot de passe incorrect");
        // Nettoie l'erreur de la session après l'avoir récupérée
        request.getSession().removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
      }

      return "custom_login";
    }
  }
