package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.FeuilleHeureRequest;
import com.ism.rhconnect.dto.response.FeuilleHeureResponse;
import com.ism.rhconnect.dto.response.LigneHeureResponse;
import com.ism.rhconnect.entity.Contrat;
import com.ism.rhconnect.entity.FeuilleHeure;
import com.ism.rhconnect.entity.Notification;
import com.ism.rhconnect.entity.Utilisateur;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.exception.UnauthorizedException;
import com.ism.rhconnect.repository.ContratRepository;
import com.ism.rhconnect.repository.FeuilleHeureRepository;
import com.ism.rhconnect.repository.UtilisateurRepository;
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
    private final ContratRepository contratRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    /* ── Sprint 2 : Mouhamad — création d'un nouveau relevé mensuel ── */

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

    /* ── Lecture ── */

    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> listerParAttache(Long attacheId) {
        return feuilleHeureRepository.findByAttacheId(attacheId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> mesMesReleves() {
        Utilisateur u = getUtilisateurConnecte();
        return feuilleHeureRepository.findByAttacheId(u.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> listerSoumis() {
        return feuilleHeureRepository.findByStatut(FeuilleHeure.Statut.SOUMIS)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeuilleHeureResponse trouverParId(Long id) {
        return toResponse(feuilleHeureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + id)));
    }

    /* ── Validation / Rejet ── */

    @Transactional
    public FeuilleHeureResponse valider(Long id) {
        FeuilleHeure feuille = feuilleHeureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + id));

        if (feuille.getStatut() != FeuilleHeure.Statut.SOUMIS) {
            throw new IllegalStateException("Ce relevé doit être soumis avant validation (statut : " + feuille.getStatut() + ")");
        }

        // Calculer le total des heures
        double total = (feuille.getLignes() != null)
                ? feuille.getLignes().stream().mapToDouble(l -> l.getDuree() != null ? l.getDuree() : 0).sum()
                : 0;
        feuille.setTotalHeuresValidees(total);
        feuille.setStatut(FeuilleHeure.Statut.VALIDE);
        feuille.setDateValidation(LocalDateTime.now());

        FeuilleHeure saved = feuilleHeureRepository.save(feuille);

        notificationService.creer(
                feuille.getAttache(),
                Notification.Type.RELEVE_VALIDE,
                "Votre relevé de " + feuille.getPeriode() + " pour le module "
                        + feuille.getContrat().getModule() + " a été validé. Total : "
                        + String.format("%.1f h", total));

        return toResponse(saved);
    }

    @Transactional
    public FeuilleHeureResponse rejeter(Long id, String motif) {
        FeuilleHeure feuille = feuilleHeureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + id));

        if (feuille.getStatut() != FeuilleHeure.Statut.SOUMIS) {
            throw new IllegalStateException("Ce relevé doit être soumis avant rejet (statut : " + feuille.getStatut() + ")");
        }

        feuille.setStatut(FeuilleHeure.Statut.REJETE);
        FeuilleHeure saved = feuilleHeureRepository.save(feuille);

        String msgMotif = (motif != null && !motif.isBlank()) ? " Motif : " + motif : "";
        notificationService.creer(
                feuille.getAttache(),
                Notification.Type.RELEVE_REJETE,
                "Votre relevé de " + feuille.getPeriode() + " pour le module "
                        + feuille.getContrat().getModule() + " a été rejeté." + msgMotif);

        return toResponse(saved);
    }

    /* ── Soumission ── */

    @Transactional
    public FeuilleHeureResponse soumettre(Long id) {
        FeuilleHeure feuille = feuilleHeureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + id));

        if (feuille.getStatut() != FeuilleHeure.Statut.EN_COURS) {
            throw new IllegalStateException(
                    "Ce relevé ne peut pas être soumis (statut : " + feuille.getStatut() + ")");
        }

        feuille.setStatut(FeuilleHeure.Statut.SOUMIS);
        feuille.setDateSoumission(LocalDateTime.now());
        FeuilleHeure saved = feuilleHeureRepository.save(feuille);

        // Notifier l'attaché de classe
        notificationService.creer(
                feuille.getAttache(),
                Notification.Type.RELEVE_SOUMIS,
                "Votre relevé de " + feuille.getPeriode() + " pour le module "
                        + feuille.getContrat().getModule() + " a été soumis avec succès.");

        return toResponse(saved);
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
                .statut(f.getStatut())
                .dateSoumission(f.getDateSoumission())
                .dateValidation(f.getDateValidation())
                .lignes(lignes)
                .build();
    }

    private Utilisateur getUtilisateurConnecte() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Utilisateur introuvable"));
    }
}
