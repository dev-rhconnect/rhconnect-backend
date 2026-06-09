package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.SeanceProgrammeeRequest;
import com.ism.rhconnect.dto.response.SeanceProgrammeeResponse;
import com.ism.rhconnect.entity.*;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeanceProgrammeeService {

    private final SeanceProgrammeeRepository seanceRepository;
    private final ContratRepository contratRepository;
    private final DisponibiliteRepository disponibiliteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VacataireRepository vacataireRepository;
    private final NotificationService notificationService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /** RP crée une séance programmée dans l'emploi du temps. */
    @Transactional
    public SeanceProgrammeeResponse creer(SeanceProgrammeeRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur creePar = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Contrat contrat = contratRepository.findById(request.getContratId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable"));

        // La date doit être dans la période du contrat
        LocalDate dateSeance = request.getDateSeance();
        if (dateSeance.isBefore(contrat.getDateDebut()) || dateSeance.isAfter(contrat.getDateFin())) {
            throw new IllegalArgumentException(
                "La date " + dateSeance + " est hors de la période du contrat ("
                + contrat.getDateDebut() + " → " + contrat.getDateFin() + ").");
        }

        double duree = calculerDuree(request.getHeureDebut(), request.getHeureFin());

        SeanceProgrammee.SeanceProgrammeeBuilder builder = SeanceProgrammee.builder()
                .contrat(contrat)
                .dateSeance(request.getDateSeance())
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .duree(duree)
                .typeSeance(request.getTypeSeance())
                .salle(request.getSalle())
                .creePar(creePar);

        if (request.getDisponibiliteId() != null) {
            Disponibilite dispo = disponibiliteRepository.findById(request.getDisponibiliteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Disponibilité introuvable"));
            builder.disponibilite(dispo);
            dispo.setStatut(Disponibilite.StatutDisponibilite.CONFIRMEE);
            disponibiliteRepository.save(dispo);
        }

        SeanceProgrammee saved = seanceRepository.save(builder.build());

        // Notifier le vacataire
        notificationService.creer(
                contrat.getVacataire().getUtilisateur(),
                Notification.Type.NOUVEAU_COMPTE,
                "Une séance a été programmée le " + request.getDateSeance()
                        + " de " + request.getHeureDebut() + " à " + request.getHeureFin()
                        + " pour le module " + contrat.getModule() + ".");

        return toResponse(saved);
    }

    /** Attaché : liste les séances de la semaine courante (ou d'une semaine donnée). */
    @Transactional(readOnly = true)
    public List<SeanceProgrammeeResponse> semaine(LocalDate reference) {
        LocalDate lundi = (reference != null ? reference : LocalDate.now())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate dimanche = lundi.plusDays(6);
        return seanceRepository.findByDateSeanceBetween(lundi, dimanche)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Attaché : valide une séance réalisée. */
    @Transactional
    public SeanceProgrammeeResponse valider(Long id, String noteInterne) {
        SeanceProgrammee s = findOrThrow(id);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur valideur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        s.setStatut(SeanceProgrammee.StatutSeance.REALISEE);
        s.setValidePar(valideur);
        s.setDateValidation(LocalDateTime.now());
        if (noteInterne != null && !noteInterne.isBlank()) {
            s.setNoteInterne(noteInterne);
        }
        return toResponse(seanceRepository.save(s));
    }

    /** Attaché : annule une séance (non réalisée). */
    @Transactional
    public SeanceProgrammeeResponse annuler(Long id, String motif) {
        SeanceProgrammee s = findOrThrow(id);
        s.setStatut(SeanceProgrammee.StatutSeance.ANNULEE);
        if (motif != null && !motif.isBlank()) {
            s.setNoteInterne(motif);
        }
        return toResponse(seanceRepository.save(s));
    }

    /** Attaché : upload la feuille de présence PDF. */
    @Transactional
    public SeanceProgrammeeResponse uploadFeuillePresence(Long id, MultipartFile file) throws IOException {
        SeanceProgrammee s = findOrThrow(id);

        String originalName = file.getOriginalFilename();
        String extension = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.')) : ".pdf";

        Path dir = Paths.get(uploadDir, "presences");
        Files.createDirectories(dir);
        Path dest = dir.resolve("presence_seance_" + id + "_" + System.currentTimeMillis() + extension);
        Files.write(dest, file.getBytes());

        s.setFeuillePresencePath(dest.toString());
        return toResponse(seanceRepository.save(s));
    }

    /** RP / Admin : toutes les séances. */
    @Transactional(readOnly = true)
    public List<SeanceProgrammeeResponse> listerTous() {
        return seanceRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Vacataire : ses propres séances. */
    @Transactional(readOnly = true)
    public List<SeanceProgrammeeResponse> mesSeances() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return seanceRepository.findByContratVacataireUtilisateurId(u.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /* ── Helpers ── */

    private double calculerDuree(java.time.LocalTime debut, java.time.LocalTime fin) {
        long minutes = java.time.Duration.between(debut, fin).toMinutes();
        return minutes / 60.0;
    }

    private SeanceProgrammee findOrThrow(Long id) {
        return seanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Séance introuvable : " + id));
    }

    private SeanceProgrammeeResponse toResponse(SeanceProgrammee s) {
        Utilisateur u = s.getContrat().getVacataire().getUtilisateur();
        return SeanceProgrammeeResponse.builder()
                .id(s.getId())
                .contratId(s.getContrat().getId())
                .nomVacataire(u.getPrenom() + " " + u.getNom())
                .emailVacataire(u.getEmail())
                .specialiteVacataire(s.getContrat().getVacataire().getSpecialite())
                .module(s.getContrat().getModule())
                .classe(s.getContrat().getClasse())
                .dateSeance(s.getDateSeance())
                .heureDebut(s.getHeureDebut())
                .heureFin(s.getHeureFin())
                .duree(s.getDuree())
                .typeSeance(s.getTypeSeance())
                .salle(s.getSalle())
                .statut(s.getStatut())
                .feuillePresenceUploaded(s.getFeuillePresencePath() != null)
                .noteInterne(s.getNoteInterne())
                .nomValidePar(s.getValidePar() != null
                        ? s.getValidePar().getPrenom() + " " + s.getValidePar().getNom() : null)
                .dateValidation(s.getDateValidation())
                .dateCreation(s.getDateCreation())
                .build();
    }
}
