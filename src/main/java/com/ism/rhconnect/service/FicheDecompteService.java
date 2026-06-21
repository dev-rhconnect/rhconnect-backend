package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.FicheDecompteRequest;
import com.ism.rhconnect.dto.response.FicheDecompteResponse;
import com.ism.rhconnect.entity.*;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FicheDecompteService {

    private final FicheDecompteRepository ficheRepository;
    private final VacataireRepository vacataireRepository;
    private final ContratModuleRepository contratModuleRepository;
    private final SeanceProgrammeeRepository seanceProgrammeeRepository;

    /** Initialiser ou récupérer la fiche du mois pour un module/vacataire. */
    @Transactional
    public FicheDecompteResponse obtenirFicheMois(Long vacataireId, Long contratModuleId, YearMonth mois) {
        Vacataire vac = vacataireRepository.findById(vacataireId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacataire introuvable"));
        ContratModule cm = contratModuleRepository.findById(contratModuleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module de contrat introuvable"));

        FicheDecompte fiche = ficheRepository.findByVacataireIdAndContratModuleIdAndMois(
                vacataireId, contratModuleId, mois)
                .orElseGet(() -> {
                    FicheDecompte newFiche = FicheDecompte.builder()
                            .vacataire(vac)
                            .contratModule(cm)
                            .mois(mois)
                            .statut(FicheDecompte.StatutFiche.EN_COURS)
                            .build();
                    return ficheRepository.save(newFiche);
                });

        return toResponse(fiche);
    }

    /** Compiler la fiche du mois : calculer durée totale et montant. */
    @Transactional
    public FicheDecompteResponse compilerFiche(Long ficheId) {
        FicheDecompte fiche = ficheRepository.findById(ficheId)
                .orElseThrow(() -> new ResourceNotFoundException("Fiche introuvable"));

        // Calculer durée totale des séances réalisées ce mois (via le contrat du module)
        Long contratId = fiche.getContratModule().getContrat().getId();
        List<SeanceProgrammee> seances = seanceProgrammeeRepository.findAll().stream()
                .filter(s -> s.getContrat().getId().equals(contratId)
                        && s.getStatut() == SeanceProgrammee.StatutSeance.REALISEE
                        && s.getDateSeance().getYear() == fiche.getMois().getYear()
                        && s.getDateSeance().getMonthValue() == fiche.getMois().getMonthValue())
                .collect(Collectors.toList());

        double dureeTotal = seances.stream()
                .mapToDouble(SeanceProgrammee::getDuree)
                .sum();

        TypeVacataire type = fiche.getVacataire().getTypeVacataire();
        if (type == null) type = TypeVacataire.STANDARD;
        double tauxHoraire = ContratService.computeTauxHoraire(
                type,
                fiche.getContratModule().getNiveau());

        double montantBrut = dureeTotal * tauxHoraire;

        fiche.setDureeTotal(dureeTotal);
        fiche.setMontantBrut(montantBrut);
        fiche.setStatut(FicheDecompte.StatutFiche.COMPILEE);
        fiche.setDateCompilation(java.time.LocalDate.now());
        fiche.setValideeParAttache(true);

        return toResponse(ficheRepository.save(fiche));
    }

    /** Relais Finance : valider la fiche. */
    @Transactional
    public FicheDecompteResponse validerFiche(Long ficheId) {
        FicheDecompte fiche = ficheRepository.findById(ficheId)
                .orElseThrow(() -> new ResourceNotFoundException("Fiche introuvable"));

        if (fiche.getStatut() != FicheDecompte.StatutFiche.COMPILEE) {
            throw new IllegalStateException("La fiche doit être compilée avant validation");
        }

        fiche.setStatut(FicheDecompte.StatutFiche.VALIDEE_FINANCE);
        fiche.setValideeParRelaisFinance(true);
        fiche.setDateValidation(java.time.LocalDate.now());

        return toResponse(ficheRepository.save(fiche));
    }

    /** Lister les fiches compilées (prêtes pour validation Relais Finance). */
    @Transactional(readOnly = true)
    public List<FicheDecompteResponse> fichesPourValidation(YearMonth mois) {
        return ficheRepository.findByMois(mois).stream()
                .filter(f -> f.getStatut() == FicheDecompte.StatutFiche.COMPILEE)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Lister les fiches du mois (pour Attaché). */
    @Transactional(readOnly = true)
    public List<FicheDecompteResponse> fichesDuMois(YearMonth mois) {
        return ficheRepository.findByMois(mois).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private FicheDecompteResponse toResponse(FicheDecompte f) {
        Utilisateur u = f.getVacataire().getUtilisateur();
        ContratModule cm = f.getContratModule();
        TypeVacataire type = f.getVacataire().getTypeVacataire();
        if (type == null) type = TypeVacataire.STANDARD;
        double tauxHoraire = ContratService.computeTauxHoraire(
                type, cm.getNiveau());

        return FicheDecompteResponse.builder()
                .id(f.getId())
                .vacataireId(f.getVacataire().getId())
                .nomVacataire(u.getPrenom() + " " + u.getNom())
                .emailVacataire(u.getEmail())
                .contratModuleId(cm.getId())
                .nomModule(cm.getNomModule())
                .classes(String.join(", ", cm.getClasses()))
                .niveau(cm.getNiveau().name())
                .mois(f.getMois())
                .statut(f.getStatut())
                .dureeTotal(f.getDureeTotal())
                .montantBrut(f.getMontantBrut())
                .tauxHoraire(tauxHoraire)
                .valideeParAttache(f.isValideeParAttache())
                .valideeParRelaisFinance(f.isValideeParRelaisFinance())
                .dateCompilation(f.getDateCompilation())
                .dateValidation(f.getDateValidation())
                .dateCreation(f.getDateCreation())
                .build();
    }
}
