package com.genuinecoder.learnspringsecurity;

import com.genuinecoder.learnspringsecurity.model.HistoriquePlanning;
import com.genuinecoder.learnspringsecurity.model.HistoriquePlanningRepository;
import com.genuinecoder.learnspringsecurity.model.MyUser;
import com.genuinecoder.learnspringsecurity.model.MyUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/historique")
public class HistoriqueController {

    @Autowired
    private HistoriquePlanningRepository historiqueRepo;

    @Autowired
    private MyUserRepository userRepo;

    @GetMapping
    public String getHistorique(Authentication authentication, Model model) {
        String email = authentication.getName();
        MyUser currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        List<HistoriquePlanning> historique = historiqueRepo.findByUtilisateurIdOrderByDateActionDesc(currentUser.getId());
        model.addAttribute("historique", historique);

        return "historique";
    }
}
