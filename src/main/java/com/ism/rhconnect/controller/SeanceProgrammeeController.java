package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.SeanceProgrammeeRequest;
import com.ism.rhconnect.dto.response.ModuleActifParClasseResponse;
import com.ism.rhconnect.dto.response.SeanceProgrammeeResponse;
import com.ism.rhconnect.service.SeanceProgrammeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/seances")
@RequiredArgsConstructor
public class SeanceProgrammeeController {

    private final SeanceProgrammeeService seanceService;

    /** RP crée une séance dans l'emploi du temps. */
    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<SeanceProgrammeeResponse> creer(
            @Valid @RequestBody SeanceProgrammeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seanceService.creer(request));
    }

    /** Attaché : séances REALISEE d'un contrat pour une période donnée (ex: 2026-06). */
    @GetMapping("/realisees")
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<List<SeanceProgrammeeResponse>> realiseesPourPeriode(
            @RequestParam Long contratId,
            @RequestParam String periode) {
        return ResponseEntity.ok(seanceService.realiseesPourPeriode(contratId, periode));
    }

    /** Attaché / RP : séances de la semaine (optionnel: ?reference=2026-06-09&classeNom=L1-INFO). */
    @GetMapping("/semaine")
    @PreAuthorize("hasAnyRole('ATTACHE_CLASSE', 'RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<List<SeanceProgrammeeResponse>> semaine(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reference,
            @RequestParam(required = false) String classeNom) {
        return ResponseEntity.ok(seanceService.semaine(reference, classeNom));
    }

    /** Toutes les séances (RP, Admin, Attaché pour le dashboard). */
    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN', 'ATTACHE_CLASSE')")
    public ResponseEntity<List<SeanceProgrammeeResponse>> listerTous() {
        return ResponseEntity.ok(seanceService.listerTous());
    }

    /** Vacataire : ses propres séances. */
    @GetMapping("/mes-seances")
    @PreAuthorize("hasRole('VACATAIRE')")
    public ResponseEntity<List<SeanceProgrammeeResponse>> mesSeances() {
        return ResponseEntity.ok(seanceService.mesSeances());
    }

    /** Attaché : valide une séance (marque comme REALISEE). */
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<SeanceProgrammeeResponse> valider(
            @PathVariable Long id,
            @RequestParam(required = false) String noteInterne) {
        return ResponseEntity.ok(seanceService.valider(id, noteInterne));
    }

    /** Attaché : annule une séance. */
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<SeanceProgrammeeResponse> annuler(
            @PathVariable Long id,
            @RequestParam(required = false) String motif) {
        return ResponseEntity.ok(seanceService.annuler(id, motif));
    }

    /** Planning : modules EN_COURS d'un contrat ACTIF pour une classe donnée. */
    @GetMapping("/modules-actifs")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN', 'ATTACHE_CLASSE')")
    public ResponseEntity<List<ModuleActifParClasseResponse>> modulesActifsParClasse(
            @RequestParam String classeNom) {
        return ResponseEntity.ok(seanceService.modulesActifsParClasse(classeNom));
    }

    /** Attaché : upload la feuille de présence. */
    @PostMapping("/{id}/feuille-presence")
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<SeanceProgrammeeResponse> uploadFeuillePresence(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(seanceService.uploadFeuillePresence(id, file));
    }
}
