package com.genuinecoder.learnspringsecurity;

import com.genuinecoder.learnspringsecurity.model.HistoriquePlanning;
import com.genuinecoder.learnspringsecurity.model.HistoriquePlanningRepository;
import com.genuinecoder.learnspringsecurity.model.MyUser;
import com.genuinecoder.learnspringsecurity.model.PlanningDeTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HistoriquePlanningService {
    @Autowired
    private HistoriquePlanningRepository historiqueRepo;

    public void enregistrerAction(PlanningDeTest planning, MyUser utilisateur, String action, String details) {
        HistoriquePlanning historique = new HistoriquePlanning();

        historique.setPlanningReference("Planning " + planning.getId());
        historique.setUtilisateur(utilisateur);
        historique.setAction(action);
        historique.setDetails(details);
        historique.setDateAction(LocalDateTime.now());

        historiqueRepo.save(historique);
    }

    public void enregistrerModification(PlanningDeTest ancien, PlanningDeTest nouveau, MyUser utilisateur) {
        StringBuilder ancienne = new StringBuilder();
        StringBuilder nouvelle = new StringBuilder();

        if (!ancien.getTache().equals(nouveau.getTache())) {
            ancienne.append("Tâche: ").append(ancien.getTache()).append("\n");
            nouvelle.append("Tâche: ").append(nouveau.getTache()).append("\n");
        }

        if (!ancien.getDateDebut().equals(nouveau.getDateDebut())) {
            ancienne.append("Date début: ").append(ancien.getDateDebut()).append("\n");
            nouvelle.append("Date début: ").append(nouveau.getDateDebut()).append("\n");
        }

        if (!ancien.getDateFin().equals(nouveau.getDateFin())) {
            ancienne.append("Date fin: ").append(ancien.getDateFin()).append("\n");
            nouvelle.append("Date fin: ").append(nouveau.getDateFin()).append("\n");
        }

        if (!ancien.getCommentaire().equals(nouveau.getCommentaire())) {
            ancienne.append("Commentaire: ").append(ancien.getCommentaire()).append("\n");
            nouvelle.append("Commentaire: ").append(nouveau.getCommentaire()).append("\n");
        }

        HistoriquePlanning historique = new HistoriquePlanning();
        historique.setPlanning(nouveau);
        historique.setUtilisateur(utilisateur);
        historique.setAction("MODIFICATION");
        historique.setDetails("Modification du planning ID: " + nouveau.getId());

        historique.setAncienneValeur(ancienne.toString());
        historique.setNouvelleValeur(nouvelle.toString());
        historique.setDateAction(LocalDateTime.now());

        historiqueRepo.save(historique);
    }
}