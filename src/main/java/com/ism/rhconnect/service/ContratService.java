package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.ContratModuleRequest;
import com.ism.rhconnect.dto.request.ContratRequest;
import com.ism.rhconnect.dto.response.ContratModuleResponse;
import com.ism.rhconnect.dto.response.ContratResponse;
import com.ism.rhconnect.entity.*;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContratService {

    private final ContratRepository contratRepository;
    private final VacataireRepository vacataireRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MaquetteModuleRepository maquetteModuleRepository;
    private final PdfContratService pdfContratService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /* ── CRUD ── */

    @Transactional
    public ContratResponse creer(ContratRequest req) {
        if (req.getDateFin().isBefore(req.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début");
        }

        Vacataire vacataire = vacataireRepository.findById(req.getVacataireId())
                .orElseThrow(() -> new ResourceNotFoundException("Vacataire introuvable"));

        String anneeAcad = req.getAnneeAcademique();
        if (anneeAcad == null || anneeAcad.isBlank()) {
            int annee = req.getDateDebut().getYear();
            anneeAcad = (req.getDateDebut().getMonthValue() >= 8 ? annee : annee - 1)
                    + "-" + (req.getDateDebut().getMonthValue() >= 8 ? annee + 1 : annee);
        }

        Contrat.ContratBuilder builder = Contrat.builder()
                .vacataire(vacataire)
                .tauxHoraire(req.getTauxHoraire())
                .dateDebut(req.getDateDebut())
                .dateFin(req.getDateFin())
                .anneeAcademique(anneeAcad)
                .statut(Contrat.StatutContrat.ACTIF)
                .estAvenant(req.isEstAvenant());

        if (req.getContratParentId() != null) {
            Contrat parent = contratRepository.findById(req.getContratParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contrat parent introuvable"));
            builder.contratParent(parent);
        }

        Contrat contrat = contratRepository.save(builder.build());

        // Créer les ContratModule en déduisant le VH depuis la maquette
        for (ContratModuleRequest modReq : req.getModules()) {
            double vh = modReq.getClasses().stream().mapToDouble(classe -> {
                return maquetteModuleRepository
                        .findByClasseRefNomAndModuleRefNom(classe, modReq.getNomModule())
                        .map(m -> (double) m.getVolumeHoraire())
                        .orElse(0.0);
            }).sum();

            ContratModule cm = ContratModule.builder()
                    .contrat(contrat)
                    .nomModule(modReq.getNomModule())
                    .classes(modReq.getClasses())
                    .niveau(modReq.getNiveau())
                    .estTroncCommun(modReq.isEstTroncCommun())
                    .volumeHorairePrevisionnel(vh > 0 ? vh : 1.0)
                    .heuresEffectuees(0.0)
                    .heuresRestantes(vh > 0 ? vh : 1.0)
                    .build();
            contrat.getModules().add(cm);
        }

        contratRepository.save(contrat);

        notificationService.creer(
                vacataire.getUtilisateur(),
                Notification.Type.NOUVEAU_COMPTE,
                "Un nouveau contrat " + anneeAcad + " a été créé avec "
                        + req.getModules().size() + " module(s).");

        // Générer le PDF et envoyer l'email avec identifiants + contrat
        try {
            byte[] pdf = pdfContratService.genererContrat(contrat);
            Path dir = Paths.get(uploadDir, "contrats");
            Files.createDirectories(dir);
            Path fichier = dir.resolve("contrat_" + contrat.getId() + "_" + System.currentTimeMillis() + ".pdf");
            Files.write(fichier, pdf);
            contrat.setCheminPdf(fichier.toString());
            contratRepository.save(contrat);

            Utilisateur u = vacataire.getUtilisateur();
            String nomComplet = u.getPrenom() + " " + u.getNom();
            emailService.envoyerContratAvecCredentials(
                    u.getEmail(), nomComplet, anneeAcad,
                    req.getModules().stream().map(ContratModuleRequest::getNomModule)
                            .collect(java.util.stream.Collectors.joining(", ")),
                    "Vacataire@ISM2026", pdf);
        } catch (Exception e) {
            // L'email ne doit pas bloquer la création du contrat
        }

        return toResponse(contrat);
    }

    @Transactional(readOnly = true)
    public List<ContratResponse> listerParVacataire(Long vacataireId) {
        return contratRepository.findByVacataireId(vacataireId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContratResponse> listerTous() {
        return contratRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContratResponse trouverParId(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ContratResponse resilier(Long id) {
        Contrat c = findOrThrow(id);
        c.setStatut(Contrat.StatutContrat.RESILIE);
        return toResponse(contratRepository.save(c));
    }

    /* ── PDF ── */

    @Transactional
    public byte[] genererPdf(Long id) throws Exception {
        Contrat c = findOrThrow(id);
        byte[] pdf = pdfContratService.genererContrat(c);

        Path dir = Paths.get(uploadDir, "contrats");
        Files.createDirectories(dir);
        Path fichier = dir.resolve("contrat_" + id + "_" + System.currentTimeMillis() + ".pdf");
        Files.write(fichier, pdf);

        c.setCheminPdf(fichier.toString());
        contratRepository.save(c);
        return pdf;
    }

    /* ── Email ── */

    @Transactional
    public void envoyerEmail(Long id) throws Exception {
        Contrat c = findOrThrow(id);
        byte[] pdf = pdfContratService.genererContrat(c);

        Utilisateur u = c.getVacataire().getUtilisateur();
        String nomComplet = u.getPrenom() + " " + u.getNom();
        String modules = c.getModules().stream()
                .map(ContratModule::getNomModule).collect(Collectors.joining(", "));

        emailService.envoyerContrat(u.getEmail(), nomComplet, modules, pdf);
        emailService.envoyerContrat("fatou.faye@ism.edu.sn", nomComplet, modules, pdf);

        if (c.getCheminPdf() == null) {
            Path dir = Paths.get(uploadDir, "contrats");
            Files.createDirectories(dir);
            Path fichier = dir.resolve("contrat_" + id + "_sent.pdf");
            Files.write(fichier, pdf);
            c.setCheminPdf(fichier.toString());
            contratRepository.save(c);
        }
    }

    /* ── Contrats expirants ── */

    @Transactional(readOnly = true)
    public List<ContratResponse> listerExpirants() {
        LocalDate aujourd_hui = LocalDate.now();
        LocalDate dans30jours = aujourd_hui.plusDays(30);
        return contratRepository.findContratsExpirantEntre(aujourd_hui, dans30jours)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /* ── Vacataire consulte ses propres contrats ── */

    @Transactional(readOnly = true)
    public List<ContratResponse> monContrat() {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return contratRepository.findByVacataireId(
                vacataireRepository.findByUtilisateurId(u.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Dossier vacataire introuvable"))
                        .getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /* ── Démarrer un module ── */

    @Transactional
    public ContratModuleResponse demarrerModule(Long contratId, Long moduleId, LocalDate date) {
        Contrat contrat = findOrThrow(contratId);
        ContratModule cm = contrat.getModules().stream()
                .filter(m -> m.getId().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Module introuvable dans ce contrat"));
        cm.setStatut(ContratModule.StatutModule.EN_COURS);
        cm.setDateDemarrage(date);
        contratRepository.save(contrat);
        return toModuleResponse(cm);
    }

    /* ── Mapping ── */

    private ContratResponse toResponse(Contrat c) {
        Utilisateur u = c.getVacataire().getUtilisateur();
        List<ContratModuleResponse> modules = c.getModules() == null ? List.of()
                : c.getModules().stream().map(this::toModuleResponse).collect(Collectors.toList());
        return ContratResponse.builder()
                .id(c.getId())
                .vacataireId(c.getVacataire().getId())
                .nomVacataire(u.getPrenom() + " " + u.getNom())
                .emailVacataire(u.getEmail())
                .anneeAcademique(c.getAnneeAcademique())
                .tauxHoraire(c.getTauxHoraire())
                .dateDebut(c.getDateDebut())
                .dateFin(c.getDateFin())
                .pdfGenere(c.getCheminPdf() != null)
                .estAvenant(c.isEstAvenant())
                .contratParentId(c.getContratParent() != null ? c.getContratParent().getId() : null)
                .statut(c.getStatut())
                .dateCreation(c.getDateCreation())
                .modules(modules)
                .build();
    }

    private ContratModuleResponse toModuleResponse(ContratModule m) {
        return ContratModuleResponse.builder()
                .id(m.getId())
                .nomModule(m.getNomModule())
                .classes(m.getClasses())
                .niveau(m.getNiveau())
                .estTroncCommun(m.getEstTroncCommun())
                .tauxHoraire(m.getContrat().getTauxHoraire())
                .volumeHorairePrevisionnel(m.getVolumeHorairePrevisionnel())
                .heuresEffectuees(m.getHeuresEffectuees())
                .heuresRestantes(m.getHeuresRestantes())
                .dateDemarrage(m.getDateDemarrage())
                .statut(m.getStatut())
                .build();
    }

    private Contrat findOrThrow(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable : " + id));
    }

    public static double computeTauxHoraire(TypeVacataire type, NiveauEnseignement niveau) {
        boolean estProfUniv = type == TypeVacataire.PROFESSEUR_UNIVERSITAIRE;
        return switch (niveau) {
            case MASTER, MASTER_1, MASTER_2 -> estProfUniv ? 25_000 : 20_000;
            case L3, LICENCE               -> estProfUniv ? 20_000 : 17_000;
            case L1, L2                    -> estProfUniv ? 18_000 : 15_000;
            case DUT                       -> estProfUniv ? 16_000 : 13_000;
        };
    }
}
