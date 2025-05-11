package com.genuinecoder.learnspringsecurity;

import com.genuinecoder.learnspringsecurity.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class HistoriquePlanService {
    @Autowired
    private HistoriquePlanDeTestRepository historiqueRepo;

    @Autowired
    private HistoriquePlanDeTestRepository historiquePlanDeTestRepository;


    public void enregistrerAction(PlanDeTest plan, MyUser utilisateur, String action, String details) {
        HistoriquePlanDeTest historique = new HistoriquePlanDeTest();
        historique.setPlan(plan);
        historique.setUtilisateur(utilisateur);
        historique.setAction(action);
        historique.setDetails(details);
        historique.setDateAction(LocalDateTime.now());
        historiqueRepo.save(historique);
    }

    public void enregistrerSuppression(PlanDeTest plan, MyUser user) {
        HistoriquePlanDeTest historique = new HistoriquePlanDeTest();
        historique.setPlanId(plan.getId()); // always set it explicitly
        historique.setAction("SUPPRESSION");
        historique.setDateAction(LocalDateTime.now());
        historique.setUtilisateur(user);
        historique.setDetails("Suppression du plan " + plan.getId() + " - " + plan.getTitre());
        historiquePlanDeTestRepository.save(historique);
    }


    public void enregistrerModification(PlanDeTest ancien, PlanDeTest nouveau, MyUser utilisateur) {
        StringBuilder ancienne = new StringBuilder();
        StringBuilder nouvelle = new StringBuilder();

        if (!ancien.getTitre().equals(nouveau.getTitre())) {
            ancienne.append("Titre: ").append(ancien.getTitre()).append("\n");
            nouvelle.append("Titre: ").append(nouveau.getTitre()).append("\n");
        }

        if (!ancien.getDescription().equals(nouveau.getDescription())) {
            ancienne.append("Description: ").append(ancien.getDescription()).append("\n");
            nouvelle.append("Description: ").append(nouveau.getDescription()).append("\n");
        }

        if (!ancien.getOutils().equals(nouveau.getOutils())) {
            ancienne.append("Outils: ").append(ancien.getOutils()).append("\n");
            nouvelle.append("Outils: ").append(nouveau.getOutils()).append("\n");
        }

        if (!ancien.getCommentaire().equals(nouveau.getCommentaire())) {
            ancienne.append("Commentaire: ").append(ancien.getCommentaire()).append("\n");
            nouvelle.append("Commentaire: ").append(nouveau.getCommentaire()).append("\n");
        }

        HistoriquePlanDeTest historique = new HistoriquePlanDeTest();
        historique.setPlan(nouveau);
        historique.setUtilisateur(utilisateur);
        historique.setAction("MODIFICATION");
        historique.setDetails("Modification du plan ID: " + nouveau.getId());
        historique.setAncienneValeur(ancienne.toString());
        historique.setNouvelleValeur(nouvelle.toString());
        historique.setDateAction(LocalDateTime.now());

        historiqueRepo.save(historique);
    }
}