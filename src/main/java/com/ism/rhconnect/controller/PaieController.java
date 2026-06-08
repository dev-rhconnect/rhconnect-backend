package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.PaiementRequest;
import com.ism.rhconnect.dto.response.PaiementResponse;
import com.ism.rhconnect.service.PaieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paie")
@RequiredArgsConstructor
public class PaieController {

    private final PaieService paieService;

    /**
     * Calculer la rémunération d'un relevé validé.
     * Applique la retenue fiscale 5 % (CGI art. 200) automatiquement.
     */
    @PostMapping("/calculer")
    @PreAuthorize("hasRole('RELAIS_FINANCE')")
    public ResponseEntity<PaiementResponse> calculer(@Valid @RequestBody PaiementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paieService.calculer(request));
    }

    /** Lister tous les paiements. */
    @GetMapping
    @PreAuthorize("hasAnyRole('RELAIS_FINANCE', 'ADMIN')")
    public ResponseEntity<List<PaiementResponse>> listerTous() {
        return ResponseEntity.ok(paieService.listerTous());
    }

    /** Consulter un paiement par son identifiant. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RELAIS_FINANCE', 'ADMIN', 'VACATAIRE')")
    public ResponseEntity<PaiementResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(paieService.trouverParId(id));
    }

    /** Sprint 3 — Vacataire : consulter ses propres fiches de paie. */
    @GetMapping("/mes-fiches")
    @PreAuthorize("hasRole('VACATAIRE')")
    public ResponseEntity<List<PaiementResponse>> mesFiches() {
        return ResponseEntity.ok(paieService.mesFiches());
    }

    /** Sprint 2 — Ndeye Fatou : Télécharger la fiche de paie PDF et l'envoyer par email. */
    @GetMapping("/{id}/telecharger")
    @PreAuthorize("hasAnyRole('RELAIS_FINANCE', 'ADMIN', 'VACATAIRE')")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id) throws Exception {
        byte[] pdf = paieService.genererEtTelecharger(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"fiche_paie_" + id + ".pdf\"")
                .body(pdf);
    }
}
