package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.PaiementRequest;
import com.ism.rhconnect.dto.response.PaiementResponse;
import com.ism.rhconnect.entity.FeuilleHeure;
import com.ism.rhconnect.entity.Notification;
import com.ism.rhconnect.entity.Paiement;
import com.ism.rhconnect.entity.Utilisateur;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.repository.FeuilleHeureRepository;
import com.ism.rhconnect.repository.PaiementRepository;
import com.ism.rhconnect.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaieService {

    private final PaiementRepository paiementRepository;
    private final FeuilleHeureRepository feuilleHeureRepository;
    private final PdfFichePaieService pdfFichePaieService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /* ── Calcul ── */

    @Transactional
    public PaiementResponse calculer(PaiementRequest request) {
        FeuilleHeure feuille = feuilleHeureRepository.findById(request.getFeuilleHeureId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Relevé introuvable : " + request.getFeuilleHeureId()));

        if (feuille.getStatut() != FeuilleHeure.Statut.VALIDE) {
            throw new IllegalStateException("Le relevé doit être validé avant de calculer la rémunération");
        }

        double totalHeures    = feuille.getTotalHeuresValidees();
        double tauxHoraire    = request.getTauxHoraire();
        double montantBrut    = totalHeures * tauxHoraire;
        double retenueFiscale = montantBrut * 0.05;
        double montantNet     = montantBrut - retenueFiscale;

        Paiement paiement = Paiement.builder()
                .feuilleHeure(feuille)
                .totalHeures(totalHeures)
                .tauxHoraire(tauxHoraire)
                .montantBrut(montantBrut)
                .retenueFiscale(retenueFiscale)
                .montantNet(montantNet)
                .build();

        Paiement saved = paiementRepository.save(paiement);

        // Notifier le vacataire que sa fiche de paie est disponible
        Utilisateur vacataire = feuille.getContrat().getVacataire().getUtilisateur();
        notificationService.creer(
                vacataire,
                Notification.Type.FICHE_PAIE_DISPONIBLE,
                "Votre fiche de paie pour " + feuille.getPeriode()
                        + " est disponible. Net à payer : "
                        + String.format("%.0f FCFA", montantNet));

        return toResponse(saved);
    }

    /* ── Lecture ── */

    @Transactional(readOnly = true)
    public List<PaiementResponse> listerTous() {
        return paiementRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Sprint 3 — Vacataire consulte ses propres fiches de paie. */
    @Transactional(readOnly = true)
    public List<PaiementResponse> mesFiches() {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return paiementRepository.findByFeuilleHeureContratVacataireUtilisateurId(u.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaiementResponse trouverParId(Long id) {
        return toResponse(paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable : " + id)));
    }

    /* ── Sprint 2 : Ndeye Fatou — génération et téléchargement PDF ── */

    @Transactional
    public byte[] genererEtTelecharger(Long id) throws Exception {
        Paiement p = paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable : " + id));

        byte[] pdf = pdfFichePaieService.genererFichePaie(p);

        // Sauvegarder localement
        Path dir = Paths.get(uploadDir, "fiches-paie");
        Files.createDirectories(dir);
        Path fichier = dir.resolve("fiche_paie_" + id + "_" + System.currentTimeMillis() + ".pdf");
        Files.write(fichier, pdf);
        p.setCheminFichePaie(fichier.toString());
        paiementRepository.save(p);

        // Envoyer par email au vacataire
        Utilisateur u = p.getFeuilleHeure().getContrat().getVacataire().getUtilisateur();
        emailService.envoyerFichePaie(
                u.getEmail(),
                u.getPrenom() + " " + u.getNom(),
                p.getFeuilleHeure().getPeriode(),
                pdf);

        return pdf;
    }

    /* ── Mapping ── */

    private PaiementResponse toResponse(Paiement p) {
        FeuilleHeure f = p.getFeuilleHeure();
        return PaiementResponse.builder()
                .id(p.getId())
                .feuilleHeureId(f.getId())
                .nomVacataire(f.getContrat().getVacataire().getUtilisateur().getPrenom()
                        + " " + f.getContrat().getVacataire().getUtilisateur().getNom())
                .periode(f.getPeriode())
                .totalHeures(p.getTotalHeures())
                .tauxHoraire(p.getTauxHoraire())
                .montantBrut(p.getMontantBrut())
                .retenueFiscale(p.getRetenueFiscale())
                .montantNet(p.getMontantNet())
                .statut(p.getStatut())
                .dateGeneration(p.getDateGeneration())
                .build();
    }
}
