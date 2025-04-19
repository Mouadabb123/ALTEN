package com.genuinecoder.learnspringsecurity;

import com.genuinecoder.learnspringsecurity.model.HistoriquePlanning;
import com.genuinecoder.learnspringsecurity.model.HistoriquePlanningRepository;
import com.genuinecoder.learnspringsecurity.model.MyUser;
import com.genuinecoder.learnspringsecurity.model.PlanningDeTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HistoriqueService {
    @Autowired
    private HistoriquePlanningRepository historiqueRepo;

    public void enregistrerAction(PlanningDeTest planning, MyUser utilisateur, String action,String details) {


        HistoriquePlanning historique = new HistoriquePlanning();
        historique.setPlanning(planning);
        historique.setUtilisateur(utilisateur);
        historique.setAction(action);
        historique.setDetails(details);
        historique.setDateAction(LocalDateTime.now());
        historiqueRepo.save(historique);
    }
}
