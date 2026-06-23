package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.LigneHeureRequest;
import com.ism.rhconnect.dto.response.FeuilleHeureResponse;
import com.ism.rhconnect.dto.response.LigneHeureResponse;
import com.ism.rhconnect.entity.FeuilleHeure;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.repository.FeuilleHeureRepository;
import com.ism.rhconnect.service.PdfReleveService;
import com.ism.rhconnect.service.ReleveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/releves")
@RequiredArgsConstructor
public class ReleveController {

    private final ReleveService releveService;
    private final PdfReleveService pdfReleveService;
    private final FeuilleHeureRepository feuilleHeureRepository;

    /**
     * Liste les relevés avec filtres optionnels.
     * Attaché : voit ses propres relevés.
     * RP / Admin : voit tous les relevés.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ATTACHE_CLASSE', 'RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<List<FeuilleHeureResponse>> lister(
            @RequestParam(required = false) String periode,
            @RequestParam(required = false) String classeNom,
            @RequestParam(required = false) Long vacataireId) {
        return ResponseEntity.ok(releveService.lister(periode, classeNom, vacataireId));
    }

    /** Relevés soumis au RP en attente de validation. */
    @GetMapping("/soumis-rp")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<List<FeuilleHeureResponse>> soumisRP() {
        return ResponseEntity.ok(releveService.listerSoumisRP());
    }

    /** Relevés transmis au Finance en attente de validation. */
    @GetMapping("/soumis-finance")
    @PreAuthorize("hasAnyRole('RELAIS_FINANCE', 'RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<List<FeuilleHeureResponse>> soumisFinance() {
        return ResponseEntity.ok(releveService.listerSoumisFinance());
    }

    /** Relevés validés du vacataire connecté (pour son espace perso). */
    @GetMapping("/mes-releves-valides")
    @PreAuthorize("hasRole('VACATAIRE')")
    public ResponseEntity<List<FeuilleHeureResponse>> mesRelevesValides() {
        return ResponseEntity.ok(releveService.mesRelevesValides());
    }

    /** RP : tous les relevés de l'équipe (hors EN_COURS). */
    @GetMapping("/equipe")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<List<FeuilleHeureResponse>> equipe() {
        return ResponseEntity.ok(releveService.listerEquipeRP());
    }

    /** Consulter un relevé par son id. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATTACHE_CLASSE', 'RESPONSABLE_PROGRAMME', 'RELAIS_FINANCE', 'ADMIN', 'VACATAIRE')")
    public ResponseEntity<FeuilleHeureResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(releveService.trouverParId(id));
    }

    /** Ajouter une ligne manuellement à un relevé EN_COURS. */
    @PostMapping("/{id}/lignes")
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<LigneHeureResponse> ajouterLigne(
            @PathVariable Long id,
            @Valid @RequestBody LigneHeureRequest request) {
        return ResponseEntity.ok(releveService.ajouterLigne(id, request));
    }

    /* ── Workflow ── */

    /** Attaché soumet le relevé au Responsable de Programme. */
    @PatchMapping("/{id}/soumettre")
    @PreAuthorize("hasRole('ATTACHE_CLASSE')")
    public ResponseEntity<FeuilleHeureResponse> soumettre(@PathVariable Long id) {
        return ResponseEntity.ok(releveService.soumettre(id));
    }

    /** RP valide le relevé soumis par l'attaché. */
    @PatchMapping("/{id}/valider-rp")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<FeuilleHeureResponse> validerParRP(@PathVariable Long id) {
        return ResponseEntity.ok(releveService.validerParRP(id));
    }

    /** RP transmet le relevé validé au Relais Finance. */
    @PatchMapping("/{id}/soumettre-finance")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<FeuilleHeureResponse> soumettreAFinance(@PathVariable Long id) {
        return ResponseEntity.ok(releveService.soumettreAFinance(id));
    }

    /** Relais Finance valide et déclenche le paiement. */
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasRole('RELAIS_FINANCE')")
    public ResponseEntity<FeuilleHeureResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(releveService.valider(id));
    }

    /** Rejeter un relevé (Finance ou RP). */
    @PatchMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyRole('RELAIS_FINANCE', 'RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<FeuilleHeureResponse> rejeter(
            @PathVariable Long id,
            @RequestParam(required = false) String motif) {
        return ResponseEntity.ok(releveService.rejeter(id, motif));
    }

    /** Télécharger la fiche de décompte horaire en PDF. */
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ATTACHE_CLASSE', 'RESPONSABLE_PROGRAMME', 'RELAIS_FINANCE', 'ADMIN', 'VACATAIRE')")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> telechargerPdf(@PathVariable Long id) {
        try {
            FeuilleHeure feuille = feuilleHeureRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + id));
            byte[] pdf = pdfReleveService.genererFicheDecompte(feuille);
            String nom = "fiche_decompte_" + feuille.getPeriode() + "_"
                    + feuille.getContrat().getVacataire().getUtilisateur().getNom().replace(" ", "_")
                    + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nom + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur génération PDF relevé {} : {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** RP répond à une demande d'explication du Finance. */
    @PatchMapping("/{id}/reponse-explication")
    @PreAuthorize("hasRole('RESPONSABLE_PROGRAMME')")
    public ResponseEntity<FeuilleHeureResponse> repondreExplication(
            @PathVariable Long id,
            @RequestParam String reponse) {
        return ResponseEntity.ok(releveService.repondreExplication(id, reponse));
    }
}
