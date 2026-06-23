package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.SeanceProgrammeeRequest;
import com.ism.rhconnect.dto.response.ModuleActifParClasseResponse;
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
    private final ContratModuleRepository contratModuleRepository;
    private final DisponibiliteRepository disponibiliteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VacataireRepository vacataireRepository;
    private final NotificationService notificationService;
    private final ReleveService releveService;

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
                .justificationEcart(request.getJustificationEcart())
                .creePar(creePar);

        // Lier au ContratModule si fourni
        ContratModule contratModule = null;
        if (request.getContratModuleId() != null) {
            contratModule = contratModuleRepository.findById(request.getContratModuleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Module de contrat introuvable"));
            builder.contratModule(contratModule);
            // Incrémenter les heures effectuées
            contratModule.setHeuresEffectuees(contratModule.getHeuresEffectuees() + duree);
            contratModule.setHeuresRestantes(Math.max(0, contratModule.getHeuresRestantes() - duree));
            contratModuleRepository.save(contratModule);
        }

        if (request.getDisponibiliteId() != null) {
            Disponibilite dispo = disponibiliteRepository.findById(request.getDisponibiliteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Disponibilité introuvable"));
            builder.disponibilite(dispo);
            dispo.setStatut(Disponibilite.StatutDisponibilite.CONFIRMEE);
            disponibiliteRepository.save(dispo);
        }

        SeanceProgrammee saved = seanceRepository.save(builder.build());

        // Notifier le vacataire
        String nomModule = contratModule != null ? contratModule.getNomModule() : "—";
        notificationService.creer(
                contrat.getVacataire().getUtilisateur(),
                Notification.Type.NOUVEAU_COMPTE,
                "Une séance a été programmée le " + request.getDateSeance()
                        + " de " + request.getHeureDebut() + " à " + request.getHeureFin()
                        + " pour le module " + nomModule + ".");

        return toResponse(saved);
    }

    /** Attaché / RP : liste les séances de la semaine, avec filtres optionnels. */
    @Transactional(readOnly = true)
    public List<SeanceProgrammeeResponse> semaine(LocalDate reference, String classeNom) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur appelant = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        LocalDate lundi = (reference != null ? reference : LocalDate.now())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate dimanche = lundi.plusDays(6);

        List<SeanceProgrammee> seances = seanceRepository.findByDateSeanceBetween(lundi, dimanche);

        // Filtre par classe explicite uniquement si fourni (le filtrage niveau est fait côté frontend)
        if (classeNom != null && !classeNom.isBlank()) {
            seances = seances.stream()
                    .filter(s -> classeCorrespond(s, classeNom))
                    .collect(Collectors.toList());
        }

        return seances.stream().map(this::toResponse).collect(Collectors.toList());
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
        SeanceProgrammee saved = seanceRepository.save(s);

        // Créer ou incrémenter le relevé mensuel automatiquement
        releveService.creerOuIncrementer(saved, valideur);

        return toResponse(saved);
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

    /** Attaché : séances REALISEE d'un contrat pour un mois donné (format "2026-06"). */
    @Transactional(readOnly = true)
    public List<SeanceProgrammeeResponse> realiseesPourPeriode(Long contratId, String periode) {
        String[] parts = periode.split("-");
        LocalDate debut = LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 1);
        LocalDate fin = debut.withDayOfMonth(debut.lengthOfMonth());
        return seanceRepository.findByContratIdAndStatutAndDateSeanceBetween(
                        contratId, SeanceProgrammee.StatutSeance.REALISEE, debut, fin)
                .stream().map(this::toResponse).collect(Collectors.toList());
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

    /**
     * Retourne les classes d'une séance, ou null si indéterminé.
     * null signifie "pas d'info de classe" — la séance doit être montrée par défaut.
     */
    private java.util.List<String> classesDeLaSeance(SeanceProgrammee s) {
        if (s.getContratModule() != null && s.getContratModule().getClasses() != null
                && !s.getContratModule().getClasses().isEmpty()) {
            return s.getContratModule().getClasses();
        }
        String ancienneClasse = s.getContrat().getClasse();
        return ancienneClasse != null ? java.util.List.of(ancienneClasse) : null;
    }

    /** Vérifie si une séance concerne la classe donnée. Si pas d'info, retourne false. */
    private boolean classeCorrespond(SeanceProgrammee s, String classeNom) {
        java.util.List<String> classes = classesDeLaSeance(s);
        return classes != null && classes.contains(classeNom);
    }

    /** Vérifie si une séance est dans le périmètre d'un niveau. Si pas d'info, retourne true (visible par défaut). */
    private boolean seanceEstDansNiveau(SeanceProgrammee s, String niveau) {
        java.util.List<String> classes = classesDeLaSeance(s);
        if (classes == null) return true; // pas d'info = on la montre
        return classes.stream().anyMatch(c -> c.toUpperCase().startsWith(niveau.toUpperCase()));
    }

    /** Vérifie que l'attaché est bien autorisé à agir sur cette séance. */
    private void verifierAutorisationNiveau(Utilisateur attache, SeanceProgrammee seance) {
        if (attache.getRole() != Role.ATTACHE_CLASSE) return;
        if (attache.getNiveauGere() == null || attache.getNiveauGere().isBlank()) return;
        // Si pas d'info de classe → on laisse passer (séances sans classe assignée)
        if (!seanceEstDansNiveau(seance, attache.getNiveauGere())) {
            throw new IllegalArgumentException(
                "Vous n'êtes pas autorisé à agir sur cette séance (hors niveau " + attache.getNiveauGere() + ").");
        }
    }

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
        ContratModule cm = s.getContratModule();
        // Nom du module et classes — depuis ContratModule (nouveaux contrats) ou fallback anciens
        String nomModule = cm != null ? cm.getNomModule() : s.getContrat().getModule();
        java.util.List<String> classes = cm != null ? cm.getClasses() : null;
        String classeAffichage = classes != null && !classes.isEmpty()
                ? String.join(", ", classes)
                : s.getContrat().getClasse();
        return SeanceProgrammeeResponse.builder()
                .id(s.getId())
                .contratId(s.getContrat().getId())
                .contratModuleId(cm != null ? cm.getId() : null)
                .vacataireId(s.getContrat().getVacataire().getId())
                .nomVacataire(u.getPrenom() + " " + u.getNom())
                .emailVacataire(u.getEmail())
                .specialiteVacataire(s.getContrat().getVacataire().getSpecialite())
                .module(nomModule)
                .classe(classeAffichage)
                .classes(classes)
                .justificationEcart(s.getJustificationEcart())
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

    /** Planning : modules EN_COURS d'un contrat ACTIF pour une classe donnée. */
    @Transactional(readOnly = true)
    public List<ModuleActifParClasseResponse> modulesActifsParClasse(String classeNom) {
        return contratModuleRepository.findModulesActifsParClasse(
                classeNom,
                Contrat.StatutContrat.ACTIF,
                ContratModule.StatutModule.EN_COURS)
            .stream()
            .map(cm -> {
                Utilisateur u = cm.getContrat().getVacataire().getUtilisateur();
                return ModuleActifParClasseResponse.builder()
                        .contratModuleId(cm.getId())
                        .contratId(cm.getContrat().getId())
                        .nomModule(cm.getNomModule())
                        .niveau(cm.getNiveau() != null ? cm.getNiveau().name() : null)
                        .classes(cm.getClasses())
                        .volumeHorairePrevisionnel(cm.getVolumeHorairePrevisionnel())
                        .heuresEffectuees(cm.getHeuresEffectuees())
                        .vacataireNom(u.getPrenom() + " " + u.getNom())
                        .vacataireId(cm.getContrat().getVacataire().getId())
                        .build();
            })
            .collect(Collectors.toList());
    }
}
