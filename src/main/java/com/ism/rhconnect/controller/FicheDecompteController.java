package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.response.FicheDecompteResponse;
import com.ism.rhconnect.service.FicheDecompteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/fiches-decompte")
@RequiredArgsConstructor
public class FicheDecompteController {

    private final FicheDecompteService ficheService;

    /** Attaché : obtenir la fiche du mois pour un vacataire/module. */
    @GetMapping("/{vacataireId}/{contratModuleId}")
    @PreAuthorize("hasAnyRole('ATTACHE_CLASSE', 'RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<FicheDecompteResponse> obtenirFiche(
            @PathVariable Long vacataireId,
            @PathVariable Long contratModuleId,
            @RequestParam(defaultValue = "current")
            @DateTimeFormat(pattern = "yyyy-MM") String mois) {
        YearMonth yearMonth = mois.equals("current") ? YearMonth.now() : YearMonth.parse(mois);
        return ResponseEntity.ok(ficheService.obtenirFicheMois(vacataireId, contratModuleId, yearMonth));
    }

    /** Attaché : compiler la fiche du mois (calculer durée total et montant). */
    @PatchMapping("/{ficheId}/compiler")
    @PreAuthorize("hasAnyRole('ATTACHE_CLASSE', 'RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<FicheDecompteResponse> compilerFiche(@PathVariable Long ficheId) {
        return ResponseEntity.ok(ficheService.compilerFiche(ficheId));
    }

    /** Relais Finance : valider la fiche compilée. */
    @PatchMapping("/{ficheId}/valider")
    @PreAuthorize("hasAnyRole('RELAIS_FINANCE', 'ADMIN')")
    public ResponseEntity<FicheDecompteResponse> validerFiche(@PathVariable Long ficheId) {
        return ResponseEntity.ok(ficheService.validerFiche(ficheId));
    }

    /** Relais Finance : lister les fiches compilées (en attente de validation). */
    @GetMapping("/pour-validation")
    @PreAuthorize("hasAnyRole('RELAIS_FINANCE', 'ADMIN')")
    public ResponseEntity<List<FicheDecompteResponse>> fichesPourValidation(
            @RequestParam(defaultValue = "current")
            @DateTimeFormat(pattern = "yyyy-MM") String mois) {
        YearMonth yearMonth = mois.equals("current") ? YearMonth.now() : YearMonth.parse(mois);
        return ResponseEntity.ok(ficheService.fichesPourValidation(yearMonth));
    }

    /** Attaché : lister toutes les fiches du mois. */
    @GetMapping("/mois")
    @PreAuthorize("hasAnyRole('ATTACHE_CLASSE', 'RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<List<FicheDecompteResponse>> fichesDuMois(
            @RequestParam(defaultValue = "current")
            @DateTimeFormat(pattern = "yyyy-MM") String mois) {
        YearMonth yearMonth = mois.equals("current") ? YearMonth.now() : YearMonth.parse(mois);
        return ResponseEntity.ok(ficheService.fichesDuMois(yearMonth));
    }
}
