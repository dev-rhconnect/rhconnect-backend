package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.response.KpiResponse;
import com.ism.rhconnect.entity.Contrat;
import com.ism.rhconnect.entity.FeuilleHeure;
import com.ism.rhconnect.entity.Vacataire;
import com.ism.rhconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KpiService {

    private final VacataireRepository vacataireRepository;
    private final ContratRepository contratRepository;
    private final FeuilleHeureRepository feuilleHeureRepository;
    private final PaiementRepository paiementRepository;

    @Transactional(readOnly = true)
    public KpiResponse calculer() {
        long vacTotal  = vacataireRepository.count();
        long vacActifs = vacataireRepository.countByStatut(Vacataire.StatutVacataire.ACTIF);

        List<Contrat> contratsActifs = contratRepository.findByStatut(Contrat.StatutContrat.ACTIF);
        long nbContratsActifs = contratsActifs.size();

        LocalDate dans30j = LocalDate.now().plusDays(30);
        long contratsExpirant = contratRepository.findContratsExpirantEntre(LocalDate.now(), dans30j).size();

        long relevesEnCours = feuilleHeureRepository.countByStatut(FeuilleHeure.Statut.EN_COURS);
        long relevesSoumis  = feuilleHeureRepository.countByStatut(FeuilleHeure.Statut.SOUMIS);

        double totalHeures = feuilleHeureRepository.findByStatut(FeuilleHeure.Statut.VALIDE)
                .stream().mapToDouble(FeuilleHeure::getTotalHeuresValidees).sum();

        double totalBrut = paiementRepository.findAll().stream()
                .mapToDouble(p -> p.getMontantBrut()).sum();
        double totalNet  = paiementRepository.findAll().stream()
                .mapToDouble(p -> p.getMontantNet()).sum();

        return KpiResponse.builder()
                .vacatairesActifs(vacActifs)
                .vacatairesTotal(vacTotal)
                .totalHeuresValidees(totalHeures)
                .totalRemunerationsBrut(totalBrut)
                .totalRemunerationsNet(totalNet)
                .relevesEnCours(relevesEnCours)
                .relevesSoumis(relevesSoumis)
                .contratsActifs(nbContratsActifs)
                .contratsExpirantBientot(contratsExpirant)
                .evolutionMensuelle(buildEvolution())
                .build();
    }

    private List<KpiResponse.StatMensuelle> buildEvolution() {
        Map<String, double[]> parMois = new LinkedHashMap<>();
        paiementRepository.findAll().forEach(p -> {
            String mois = p.getFeuilleHeure().getPeriode();
            parMois.computeIfAbsent(mois, k -> new double[]{0, 0});
            parMois.get(mois)[0] += p.getTotalHeures();
            parMois.get(mois)[1] += p.getMontantNet();
        });
        return parMois.entrySet().stream()
                .map(e -> KpiResponse.StatMensuelle.builder()
                        .mois(e.getKey())
                        .heures(e.getValue()[0])
                        .montantNet(e.getValue()[1])
                        .build())
                .collect(Collectors.toList());
    }
}
