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
}
