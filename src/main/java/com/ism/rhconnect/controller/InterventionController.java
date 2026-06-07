package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.LigneHeureRequest;
import com.ism.rhconnect.dto.response.LigneHeureResponse;
import com.ism.rhconnect.service.InterventionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interventions")
@RequiredArgsConstructor
public class InterventionController {

    private final InterventionService interventionService;

    /** Saisir une séance dans une feuille d'heures. */
    @PostMapping
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<LigneHeureResponse> saisir(@Valid @RequestBody LigneHeureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interventionService.ajouterLigne(request));
    }

    /** Lister les séances d'une feuille d'heures. */
    @GetMapping("/feuille/{feuilleHeureId}")
    @PreAuthorize("hasAnyRole('ATTACHE_CLASSE', 'RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<List<LigneHeureResponse>> listerParFeuille(@PathVariable Long feuilleHeureId) {
        return ResponseEntity.ok(interventionService.listerParFeuille(feuilleHeureId));
    }
}
