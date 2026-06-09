package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.FeuilleHeureRequest;
import com.ism.rhconnect.dto.request.LigneHeureRequest;
import com.ism.rhconnect.dto.response.FeuilleHeureResponse;
import com.ism.rhconnect.dto.response.LigneHeureResponse;
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

    /** Créer un nouveau relevé mensuel (en-tête, sans lignes). */
    @PostMapping
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<FeuilleHeureResponse> creer(@Valid @RequestBody FeuilleHeureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(releveService.creerFeuille(request));
    }

    /** Ajouter une ligne d'heure (séance) à un relevé existant. */
    @PostMapping("/{id}/lignes")
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<LigneHeureResponse> ajouterLigne(
            @PathVariable Long id,
            @Valid @RequestBody LigneHeureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(releveService.ajouterLigne(id, request));
    }

    /** Mes relevés (Attaché connecté). */
    @GetMapping
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<List<FeuilleHeureResponse>> mesReleves() {
        return ResponseEntity.ok(releveService.mesMesReleves());
    }

    /** Sprint 3 — Mes relevés validés (Vacataire connecté). */
    @GetMapping("/mes-releves-valides")
    @PreAuthorize("hasRole('VACATAIRE')")
    public ResponseEntity<List<FeuilleHeureResponse>> mesRelevesValides() {
        return ResponseEntity.ok(releveService.mesRelevesValides());
    }

    /** Sprint 3 — Relevés de l'équipe du RP connecté. */
    @GetMapping("/equipe")
    @PreAuthorize("hasRole('RESPONSABLE_PROGRAMME')")
    public ResponseEntity<List<FeuilleHeureResponse>> relevesEquipe() {
        return ResponseEntity.ok(releveService.listerEquipeRP());
    }

    /** Relevés soumis — pour validation par le Relais Finance. */
    @GetMapping("/soumis")
    @PreAuthorize("hasAnyRole('RELAIS_FINANCE', 'RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<List<FeuilleHeureResponse>> soumis() {
        return ResponseEntity.ok(releveService.listerSoumis());
    }

    /** Consulter un relevé par son identifiant. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATTACHE_CLASSE', 'RESPONSABLE_PROGRAMME', 'RELAIS_FINANCE', 'ADMIN', 'VACATAIRE')")
    public ResponseEntity<FeuilleHeureResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(releveService.trouverParId(id));
    }

    /** Soumettre un relevé pour validation. */
    @PatchMapping("/{id}/soumettre")
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<FeuilleHeureResponse> soumettre(@PathVariable Long id) {
        return ResponseEntity.ok(releveService.soumettre(id));
    }

    /** Sprint 3 — Valider un relevé soumis (Relais Finance uniquement). */
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasRole('RELAIS_FINANCE')")
    public ResponseEntity<FeuilleHeureResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(releveService.valider(id));
    }

    /** Sprint 3 — Rejeter un relevé avec motif → notifie l'Attaché et le RP. */
    @PatchMapping("/{id}/rejeter")
    @PreAuthorize("hasRole('RELAIS_FINANCE')")
    public ResponseEntity<FeuilleHeureResponse> rejeter(
            @PathVariable Long id,
            @RequestParam(required = false) String motif) {
        return ResponseEntity.ok(releveService.rejeter(id, motif));
    }

    /** Sprint 3 — RP répond à une demande d'explication → notifie le Relais Finance. */
    @PatchMapping("/{id}/reponse-explication")
    @PreAuthorize("hasRole('RESPONSABLE_PROGRAMME')")
    public ResponseEntity<FeuilleHeureResponse> repondreExplication(
            @PathVariable Long id,
            @RequestParam String reponse) {
        return ResponseEntity.ok(releveService.repondreExplication(id, reponse));
    }
}
