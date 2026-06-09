package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.VacataireRequest;
import com.ism.rhconnect.dto.response.VacataireResponse;
import com.ism.rhconnect.service.VacataireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/vacataires")
@RequiredArgsConstructor
public class VacataireController {

    private final VacataireService vacataireService;

    @PostMapping
    @PreAuthorize("hasRole('RESPONSABLE_PROGRAMME')")
    public ResponseEntity<VacataireResponse> creer(@Valid @RequestBody VacataireRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vacataireService.creer(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN', 'RELAIS_FINANCE')")
    public ResponseEntity<List<VacataireResponse>> listerTous() {
        return ResponseEntity.ok(vacataireService.listerTous());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN', 'RELAIS_FINANCE', 'VACATAIRE')")
    public ResponseEntity<VacataireResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(vacataireService.trouverParId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<VacataireResponse> modifier(@PathVariable Long id,
                                                       @Valid @RequestBody VacataireRequest request) {
        return ResponseEntity.ok(vacataireService.modifier(id, request));
    }

    @PatchMapping("/{id}/archiver")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<Void> archiver(@PathVariable Long id) {
        vacataireService.archiver(id);
        return ResponseEntity.noContent().build();
    }

    /** Sprint 2 — RP : uploader la signature électronique du vacataire. */
    @PostMapping("/{id}/signature")
    @PreAuthorize("hasRole('RESPONSABLE_PROGRAMME')")
    public ResponseEntity<VacataireResponse> uploadSignature(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(vacataireService.uploadSignature(id, file));
    }

    /** Sprint 2 — Vacataire : consulter son propre dossier. */
    @GetMapping("/mon-dossier")
    @PreAuthorize("hasRole('VACATAIRE')")
    public ResponseEntity<VacataireResponse> monDossier() {
        return ResponseEntity.ok(vacataireService.monDossier());
    }

    /** Sprint 2 — Vacataire : mettre à jour ses coordonnées bancaires. */
    @PatchMapping("/mes-coordonnees")
    @PreAuthorize("hasRole('VACATAIRE')")
    public ResponseEntity<VacataireResponse> mettreAJourCoordonnees(
            @RequestBody VacataireRequest request) {
        return ResponseEntity.ok(vacataireService.mettreAJourCoordonnees(request));
    }
}
