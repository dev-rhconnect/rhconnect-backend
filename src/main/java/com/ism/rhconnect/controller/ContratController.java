package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.ContratRequest;
import com.ism.rhconnect.dto.response.ContratResponse;
import com.ism.rhconnect.service.ContratService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sprint 2 — Mame Coumba SALL
 * Gestion des contrats de vacation : création, PDF, envoi email, avenant.
 */
@RestController
@RequestMapping("/api/contrats")
@RequiredArgsConstructor
public class ContratController {

    private final ContratService contratService;

    /** Créer un nouveau contrat ou un avenant. */
    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<ContratResponse> creer(@Valid @RequestBody ContratRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contratService.creer(request));
    }

    /** Lister tous les contrats (tous rôles habilités). */
    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN', 'RELAIS_FINANCE', 'ATTACHE_CLASSE')")
    public ResponseEntity<List<ContratResponse>> listerTous() {
        return ResponseEntity.ok(contratService.listerTous());
    }

    /** Lister les contrats d'un vacataire. */
    @GetMapping("/vacataire/{vacataireId}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN', 'RELAIS_FINANCE', 'VACATAIRE')")
    public ResponseEntity<List<ContratResponse>> listerParVacataire(@PathVariable Long vacataireId) {
        return ResponseEntity.ok(contratService.listerParVacataire(vacataireId));
    }

    /** Consulter un contrat. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN', 'VACATAIRE')")
    public ResponseEntity<ContratResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(contratService.trouverParId(id));
    }

    /** Générer le PDF du contrat et le sauvegarder. */
    @PostMapping("/{id}/generer-pdf")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<byte[]> genererPdf(@PathVariable Long id) throws Exception {
        byte[] pdf = contratService.genererPdf(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition",
                        "attachment; filename=\"Contrat_RHC-" + String.format("%05d", id) + ".pdf\"")
                .body(pdf);
    }

    /**
     * Envoyer le contrat PDF par email au vacataire et en copie à la DRH
     * (Mme Fatou Faye — fatou.faye@ism.edu.sn).
     */
    @PostMapping("/{id}/envoyer")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<Void> envoyer(@PathVariable Long id) throws Exception {
        contratService.envoyerEmail(id);
        return ResponseEntity.noContent().build();
    }

    /** Résilier un contrat. */
    @PatchMapping("/{id}/resilier")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<ContratResponse> resilier(@PathVariable Long id) {
        return ResponseEntity.ok(contratService.resilier(id));
    }

    /** Sprint 2 — Contrats expirant dans les 30 prochains jours. */
    @GetMapping("/expirants")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<List<ContratResponse>> expirants() {
        return ResponseEntity.ok(contratService.listerExpirants());
    }

    /** Sprint 2 — Vacataire consulte ses propres contrats. */
    @GetMapping("/mon-contrat")
    @PreAuthorize("hasRole('VACATAIRE')")
    public ResponseEntity<List<ContratResponse>> monContrat() {
        return ResponseEntity.ok(contratService.monContrat());
    }

    /** Sprint 2 — Vacataire télécharge le PDF de son contrat. */
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN', 'VACATAIRE')")
    public ResponseEntity<byte[]> telechargerPdf(@PathVariable Long id) throws Exception {
        byte[] pdf = contratService.genererPdf(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition",
                        "attachment; filename=\"Contrat_RHC-" + String.format("%05d", id) + ".pdf\"")
                .body(pdf);
    }
}
