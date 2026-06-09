package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.DisponibiliteRequest;
import com.ism.rhconnect.dto.response.DisponibiliteResponse;
import com.ism.rhconnect.service.DisponibiliteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disponibilites")
@RequiredArgsConstructor
public class DisponibiliteController {

    private final DisponibiliteService disponibiliteService;

    /** Vacataire déclare sa disponibilité. */
    @PostMapping
    @PreAuthorize("hasRole('VACATAIRE')")
    public ResponseEntity<DisponibiliteResponse> declarer(
            @Valid @RequestBody DisponibiliteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disponibiliteService.declarer(request));
    }

    /** Vacataire consulte ses disponibilités. */
    @GetMapping("/mes-disponibilites")
    @PreAuthorize("hasRole('VACATAIRE')")
    public ResponseEntity<List<DisponibiliteResponse>> mesDisponibilites() {
        return ResponseEntity.ok(disponibiliteService.mesDisponibilites());
    }

    /** RP liste les disponibilités d'un vacataire. */
    @GetMapping("/vacataire/{vacataireId}")
    @PreAuthorize("hasRole('RESPONSABLE_PROGRAMME')")
    public ResponseEntity<List<DisponibiliteResponse>> parVacataire(
            @PathVariable Long vacataireId) {
        return ResponseEntity.ok(disponibiliteService.parVacataire(vacataireId));
    }

    /** RP liste toutes les disponibilités. */
    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<List<DisponibiliteResponse>> listerTous() {
        return ResponseEntity.ok(disponibiliteService.listerTous());
    }

    /** RP confirme une disponibilité. */
    @PatchMapping("/{id}/confirmer")
    @PreAuthorize("hasRole('RESPONSABLE_PROGRAMME')")
    public ResponseEntity<DisponibiliteResponse> confirmer(@PathVariable Long id) {
        return ResponseEntity.ok(disponibiliteService.confirmer(id));
    }
}
