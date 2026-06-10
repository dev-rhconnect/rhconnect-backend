package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.FeuilleHeureRequest;
import com.ism.rhconnect.dto.request.LigneHeureRequest;
import com.ism.rhconnect.dto.response.FeuilleHeureResponse;
import com.ism.rhconnect.dto.response.LigneHeureResponse;
import com.ism.rhconnect.entity.*;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.exception.UnauthorizedException;
import com.ism.rhconnect.entity.Paiement;
import com.ism.rhconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReleveService {

    private final FeuilleHeureRepository feuilleHeureRepository;
    private final LigneHeureRepository ligneHeureRepository;
    private final ContratRepository contratRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final PaiementRepository paiementRepository;
    private final EmailService emailService;

    /* ── Création relevé (en-tête) ── */

    @Transactional
    public FeuilleHeureResponse creerFeuille(FeuilleHeureRequest request) {
        Utilisateur attache = getUtilisateurConnecte();

        Contrat contrat = contratRepository.findById(request.getContratId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable : " + request.getContratId()));

        if (contrat.getStatut() != Contrat.StatutContrat.ACTIF) {
            throw new IllegalStateException("Le contrat n'est plus actif");
        }

        if (feuilleHeureRepository.findByContratIdAndPeriode(
                request.getContratId(), request.getPeriode()).isPresent()) {
            throw new IllegalStateException(
                    "Un relevé existe déjà pour ce contrat et cette période : " + request.getPeriode());
        }

        FeuilleHeure feuille = FeuilleHeure.builder()
                .contrat(contrat)
                .attache(attache)
                .periode(request.getPeriode())
                .statut(FeuilleHeure.Statut.EN_COURS)
                .build();

        return toResponse(feuilleHeureRepository.save(feuille));
    }

    /* ── Sprint 3 : Ajouter une ligne d'heure (séance) ── */

    @Transactional
    public LigneHeureResponse ajouterLigne(Long feuilleId, LigneHeureRequest request) {
        FeuilleHeure feuille = feuilleHeureRepository.findById(feuilleId)
                .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + feuilleId));

        if (feuille.getStatut() != FeuilleHeure.Statut.EN_COURS) {
            throw new IllegalStateException("Impossible d'ajouter une séance : le relevé est " + feuille.getStatut());
        }

        double duree = calculerDuree(request);

        LigneHeure ligne = LigneHeure.builder()
                .feuilleHeure(feuille)
                .date(request.getDate())
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .duree(duree)
                .observation(request.isAbsence() ? "ABSENCE" + (request.getObservation() != null ? " — " + request.getObservation() : "") : request.getObservation())
                .statut(request.isAbsence() ? LigneHeure.Statut.REJETEE : LigneHeure.Statut.SAISIE)
                .build();

        LigneHeure saved = ligneHeureRepository.save(ligne);

        return LigneHeureResponse.builder()
                .id(saved.getId())
                .feuilleHeureId(feuilleId)
                .date(saved.getDate())
                .heureDebut(saved.getHeureDebut())
                .heureFin(saved.getHeureFin())
                .duree(saved.getDuree())
                .observation(saved.getObservation())
                .statut(saved.getStatut())
                .build();
    }

    /* ── Lecture ── */

    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> mesMesReleves() {
        Utilisateur u = getUtilisateurConnecte();
        return feuilleHeureRepository.findByAttacheId(u.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Sprint 3 — Vacataire consulte ses relevés validés. */
    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> mesRelevesValides() {
        Utilisateur vacataire = getUtilisateurConnecte();
        return feuilleHeureRepository
                .findByContratVacataireUtilisateurIdAndStatut(vacataire.getId(), FeuilleHeure.Statut.VALIDE)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Sprint 3 — RP consulte les relevés de son équipe. */
    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> listerEquipeRP() {
        return feuilleHeureRepository.findAll()
                .stream()
                .filter(f -> f.getStatut() != FeuilleHeure.Statut.EN_COURS)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> listerSoumis() {
        return feuilleHeureRepository.findByStatut(FeuilleHeure.Statut.SOUMIS)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeuilleHeureResponse trouverParId(Long id) {
        return toResponse(feuilleHeureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + id)));
    }

    /* ── Soumission ── */

    @Transactional
    public FeuilleHeureResponse soumettre(Long id) {
        FeuilleHeure feuille = feuilleHeureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + id));

        if (feuille.getStatut() != FeuilleHeure.Statut.EN_COURS) {
            throw new IllegalStateException("Ce relevé ne peut pas être soumis (statut : " + feuille.getStatut() + ")");
        }

        if (feuille.getLignes() == null || feuille.getLignes().isEmpty()) {
            throw new IllegalStateException("Impossible de soumettre un relevé sans séances saisies");
        }

        feuille.setStatut(FeuilleHeure.Statut.SOUMIS);
        feuille.setDateSoumission(LocalDateTime.now());
        FeuilleHeure saved = feuilleHeureRepository.save(feuille);

        notificationService.creer(
                feuille.getAttache(),
                Notification.Type.RELEVE_SOUMIS,
                "Votre relevé de " + feuille.getPeriode() + " (" + feuille.getContrat().getModule()
                        + ") a été soumis. En attente de validation par le Relais Finance.");

        Utilisateur attache = feuille.getAttache();
        emailService.envoyerReleveSoumis(
                attache.getEmail(),
                attache.getPrenom(),
                feuille.getContrat().getModule(),
                feuille.getPeriode());

        return toResponse(saved);
    }

    /* ── Sprint 3 : Validation par le Relais Finance ── */

    @Transactional
    public FeuilleHeureResponse valider(Long id) {
        FeuilleHeure feuille = feuilleHeureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + id));

        if (feuille.getStatut() != FeuilleHeure.Statut.SOUMIS) {
            throw new IllegalStateException("Ce relevé doit être soumis avant validation (statut : " + feuille.getStatut() + ")");
        }

        double total = (feuille.getLignes() != null)
                ? feuille.getLignes().stream()
                    .filter(l -> l.getStatut() != LigneHeure.Statut.REJETEE)
                    .mapToDouble(l -> l.getDuree() != null ? l.getDuree() : 0).sum()
                : 0;

        feuille.setTotalHeuresValidees(total);
        feuille.setStatut(FeuilleHeure.Statut.VALIDE);
        feuille.setDateValidation(LocalDateTime.now());
        feuille.setMotifRejet(null);

        FeuilleHeure saved = feuilleHeureRepository.save(feuille);

        // Notifier l'Attaché
        notificationService.creer(
                feuille.getAttache(),
                Notification.Type.RELEVE_VALIDE,
                "Votre relevé de " + feuille.getPeriode() + " (" + feuille.getContrat().getModule()
                        + ") a été validé. Total : " + String.format("%.1f h", total));

        Utilisateur attacheValide = feuille.getAttache();
        emailService.envoyerReleveValide(
                attacheValide.getEmail(),
                attacheValide.getPrenom(),
                feuille.getContrat().getModule(),
                feuille.getPeriode(),
                String.format("%.1f heure%s", total, total > 1 ? "s" : ""));

        // Calcul automatique de la rémunération si taux horaire défini
        Double tauxHoraire = feuille.getContrat().getTauxHoraire();
        if (tauxHoraire != null && tauxHoraire > 0 && total > 0) {
            double montantBrut    = total * tauxHoraire;
            double retenueFiscale = montantBrut * 0.05;
            double montantNet     = montantBrut - retenueFiscale;

            Paiement paiement = Paiement.builder()
                    .feuilleHeure(saved)
                    .totalHeures(total)
                    .tauxHoraire(tauxHoraire)
                    .montantBrut(montantBrut)
                    .retenueFiscale(retenueFiscale)
                    .montantNet(montantNet)
                    .build();
            paiementRepository.save(paiement);

            // Notifier le vacataire que sa fiche de paie est disponible
            Utilisateur vacataire = feuille.getContrat().getVacataire().getUtilisateur();
            notificationService.creer(
                    vacataire,
                    Notification.Type.FICHE_PAIE_DISPONIBLE,
                    "Votre fiche de paie pour " + feuille.getPeriode()
                            + " (" + feuille.getContrat().getModule() + ") est disponible."
                            + " Net à payer : " + String.format("%.0f FCFA", montantNet));
        }

        return toResponse(saved);
    }

    /* ── Sprint 3 : Rejet par le Relais Finance → demande d'explication au RP ── */

    @Transactional
    public FeuilleHeureResponse rejeter(Long id, String motif) {
        FeuilleHeure feuille = feuilleHeureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + id));

        if (feuille.getStatut() != FeuilleHeure.Statut.SOUMIS) {
            throw new IllegalStateException("Ce relevé doit être soumis avant rejet (statut : " + feuille.getStatut() + ")");
        }

        feuille.setStatut(FeuilleHeure.Statut.REJETE);
        feuille.setMotifRejet(motif);
        FeuilleHeure saved = feuilleHeureRepository.save(feuille);

        String msgMotif = (motif != null && !motif.isBlank()) ? " Motif : " + motif : "";

        // Notifier l'Attaché
        notificationService.creer(
                feuille.getAttache(),
                Notification.Type.RELEVE_REJETE,
                "Votre relevé de " + feuille.getPeriode() + " (" + feuille.getContrat().getModule()
                        + ") a été rejeté." + msgMotif);

        Utilisateur attacheRejete = feuille.getAttache();
        emailService.envoyerReleveRejete(
                attacheRejete.getEmail(),
                attacheRejete.getPrenom(),
                feuille.getContrat().getModule(),
                feuille.getPeriode(),
                motif);

        // Demande d'explication → tous les RP
        List<Utilisateur> responsables = utilisateurRepository.findByRole(Role.RESPONSABLE_PROGRAMME);
        for (Utilisateur rp : responsables) {
            notificationService.creer(
                    rp,
                    Notification.Type.DEMANDE_EXPLICATION,
                    "Le Relais Finance a rejeté le relevé de "
                            + feuille.getAttache().getPrenom() + " " + feuille.getAttache().getNom()
                            + " (" + feuille.getPeriode() + " — " + feuille.getContrat().getModule() + ")."
                            + msgMotif + " Veuillez prendre contact avec l'attaché concerné.");
        }

        return toResponse(saved);
    }

    /* ── Sprint 3 : RP répond à la demande d'explication du Relais Finance ── */

    @Transactional
    public FeuilleHeureResponse repondreExplication(Long id, String reponse) {
        FeuilleHeure feuille = feuilleHeureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + id));

        if (feuille.getStatut() != FeuilleHeure.Statut.REJETE) {
            throw new IllegalStateException("Ce relevé n'est pas en statut rejeté");
        }

        // Notifier tous les Relais Finance
        List<Utilisateur> finances = utilisateurRepository.findByRole(Role.RELAIS_FINANCE);
        Utilisateur rp = getUtilisateurConnecte();
        String msgReponse = rp.getPrenom() + " " + rp.getNom()
                + " a répondu à votre demande d'explication concernant le relevé de "
                + feuille.getAttache().getPrenom() + " " + feuille.getAttache().getNom()
                + " (" + feuille.getPeriode() + " — " + feuille.getContrat().getModule() + ") : "
                + reponse;
        for (Utilisateur finance : finances) {
            notificationService.creer(finance, Notification.Type.REPONSE_EXPLICATION, msgReponse);
        }

        return toResponse(feuille);
    }

    /* ── Mapping ── */

    private FeuilleHeureResponse toResponse(FeuilleHeure f) {
        List<LigneHeureResponse> lignes = (f.getLignes() != null)
                ? f.getLignes().stream().map(l -> LigneHeureResponse.builder()
                        .id(l.getId())
                        .feuilleHeureId(f.getId())
                        .date(l.getDate())
                        .heureDebut(l.getHeureDebut())
                        .heureFin(l.getHeureFin())
                        .duree(l.getDuree())
                        .observation(l.getObservation())
                        .statut(l.getStatut())
                        .build()).collect(Collectors.toList())
                : List.of();

        return FeuilleHeureResponse.builder()
                .id(f.getId())
                .contratId(f.getContrat().getId())
                .nomVacataire(f.getContrat().getVacataire().getUtilisateur().getPrenom()
                        + " " + f.getContrat().getVacataire().getUtilisateur().getNom())
                .module(f.getContrat().getModule())
                .classe(f.getContrat().getClasse())
                .periode(f.getPeriode())
                .totalHeuresValidees(f.getTotalHeuresValidees())
                .volumeHorairePrevisionnel(f.getContrat().getVolumeHorairePrevisionnel())
                .statut(f.getStatut())
                .dateSoumission(f.getDateSoumission())
                .dateValidation(f.getDateValidation())
                .motifRejet(f.getMotifRejet())
                .lignes(lignes)
                .build();
    }

    private double calculerDuree(LigneHeureRequest req) {
        if (req.isAbsence()) return 0.0;
        long minutes = req.getHeureDebut().until(req.getHeureFin(), java.time.temporal.ChronoUnit.MINUTES);
        return minutes / 60.0;
    }

    private Utilisateur getUtilisateurConnecte() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Utilisateur introuvable"));
    }
}
