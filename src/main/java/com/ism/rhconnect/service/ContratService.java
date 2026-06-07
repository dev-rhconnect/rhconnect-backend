package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.ContratRequest;
import com.ism.rhconnect.dto.response.ContratResponse;
import com.ism.rhconnect.entity.Contrat;
import com.ism.rhconnect.entity.Notification;
import com.ism.rhconnect.entity.Utilisateur;
import com.ism.rhconnect.entity.Vacataire;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.repository.ContratRepository;
import com.ism.rhconnect.repository.VacataireRepository;
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

        Contrat.ContratBuilder builder = Contrat.builder()
                .vacataire(vacataire)
                .module(req.getModule())
                .classe(req.getClasse())
                .volumeHorairePrevisionnel(req.getVolumeHorairePrevisionnel())
                .tauxHoraire(req.getTauxHoraire())
                .dateDebut(req.getDateDebut())
                .dateFin(req.getDateFin())
                .statut(Contrat.StatutContrat.ACTIF)
                .estAvenant(req.isEstAvenant());

        if (req.getContratParentId() != null) {
            Contrat parent = contratRepository.findById(req.getContratParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contrat parent introuvable"));
            builder.contratParent(parent);
        }

        Contrat saved = contratRepository.save(builder.build());

        // Notifier le vacataire
        notificationService.creer(
                vacataire.getUtilisateur(),
                Notification.Type.NOUVEAU_COMPTE,
                "Un nouveau contrat a été créé pour le module " + req.getModule()
                        + " (" + req.getDateDebut() + " → " + req.getDateFin() + ").");

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ContratResponse> listerParVacataire(Long vacataireId) {
        return contratRepository.findByVacataireId(vacataireId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContratResponse> listerTous() {
        return contratRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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

        // Email au vacataire
        emailService.envoyerContrat(u.getEmail(), nomComplet, c.getModule(), pdf);

        // Copie à la DRH (Mme Fatou Faye)
        emailService.envoyerContrat("fatou.faye@ism.edu.sn", nomComplet, c.getModule(), pdf);

        // Mettre à jour le PDF en base si pas encore fait
        if (c.getCheminPdf() == null) {
            Path dir = Paths.get(uploadDir, "contrats");
            Files.createDirectories(dir);
            Path fichier = dir.resolve("contrat_" + id + "_sent.pdf");
            Files.write(fichier, pdf);
            c.setCheminPdf(fichier.toString());
            contratRepository.save(c);
        }
    }

    /* ── Mapping ── */

    private ContratResponse toResponse(Contrat c) {
        Utilisateur u = c.getVacataire().getUtilisateur();
        return ContratResponse.builder()
                .id(c.getId())
                .vacataireId(c.getVacataire().getId())
                .nomVacataire(u.getPrenom() + " " + u.getNom())
                .emailVacataire(u.getEmail())
                .module(c.getModule())
                .classe(c.getClasse())
                .volumeHorairePrevisionnel(c.getVolumeHorairePrevisionnel())
                .tauxHoraire(c.getTauxHoraire())
                .dateDebut(c.getDateDebut())
                .dateFin(c.getDateFin())
                .pdfGenere(c.getCheminPdf() != null)
                .estAvenant(c.isEstAvenant())
                .contratParentId(c.getContratParent() != null ? c.getContratParent().getId() : null)
                .statut(c.getStatut())
                .dateCreation(c.getDateCreation())
                .build();
    }

    private Contrat findOrThrow(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable : " + id));
    }
}
