package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.FeuilleHeureRequest;
import com.ism.rhconnect.dto.response.FeuilleHeureResponse;
import com.ism.rhconnect.service.ReleveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/releves")
@RequiredArgsConstructor
public class ReleveController {

    private final ReleveService releveService;

    /** Sprint 2 — Créer un nouveau relevé mensuel. */
    @PostMapping
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<FeuilleHeureResponse> creer(@Valid @RequestBody FeuilleHeureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(releveService.creerFeuille(request));
    }

    /** Lister les relevés de l'Attaché de Classe connecté. */
    @GetMapping
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<List<FeuilleHeureResponse>> mesReleves() {
        return ResponseEntity.ok(releveService.mesMesReleves());
    }

    /** Lister les relevés soumis (pour validation par le RP). */
    @GetMapping("/soumis")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<List<FeuilleHeureResponse>> soumis() {
        return ResponseEntity.ok(releveService.listerSoumis());
    }

    /** Consulter un relevé par son identifiant. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATTACHE_CLASSE', 'RESPONSABLE_PROGRAMME', 'RELAIS_FINANCE', 'ADMIN')")
    public ResponseEntity<FeuilleHeureResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(releveService.trouverParId(id));
    }

    /** Soumettre un relevé pour validation. */
    @PatchMapping("/{id}/soumettre")
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<FeuilleHeureResponse> soumettre(@PathVariable Long id) {
        return ResponseEntity.ok(releveService.soumettre(id));
    }

    /** Sprint 2 — Valider un relevé soumis. */
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<FeuilleHeureResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(releveService.valider(id));
    }

    /** Sprint 2 — Rejeter un relevé soumis. */
    @PatchMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<FeuilleHeureResponse> rejeter(
            @PathVariable Long id,
            @RequestParam(required = false) String motif) {
        return ResponseEntity.ok(releveService.rejeter(id, motif));
    }
}
