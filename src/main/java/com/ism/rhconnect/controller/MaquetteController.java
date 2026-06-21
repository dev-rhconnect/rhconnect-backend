package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.MaquetteModuleRequest;
import com.ism.rhconnect.dto.response.MaquetteModuleResponse;
import com.ism.rhconnect.service.MaquetteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/maquette")
@RequiredArgsConstructor
public class MaquetteController {

    private final MaquetteService maquetteService;

    /** Lister toutes les entrées de la maquette. */
    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN', 'ATTACHE_CLASSE', 'RELAIS_FINANCE', 'VACATAIRE')")
    public ResponseEntity<List<MaquetteModuleResponse>> listerTous() {
        return ResponseEntity.ok(maquetteService.listerTous());
    }

    /** Lister les modules d'une classe spécifique avec leur VH. */
    @GetMapping("/classe/{classeNom}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN', 'ATTACHE_CLASSE', 'RELAIS_FINANCE')")
    public ResponseEntity<List<MaquetteModuleResponse>> listerParClasse(@PathVariable String classeNom) {
        return ResponseEntity.ok(maquetteService.listerParClasse(classeNom));
    }

    /** Obtenir le VH d'un module dans une classe (pour auto-remplissage lors de la création du contrat). */
    @GetMapping("/vh")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN', 'ATTACHE_CLASSE')")
    public ResponseEntity<Map<String, Object>> getVolumeHoraire(
            @RequestParam String classeNom,
            @RequestParam String moduleNom) {
        Optional<Double> vh = maquetteService.getVolumeHoraire(classeNom, moduleNom);
        return ResponseEntity.ok(Map.of(
                "classeNom", classeNom,
                "moduleNom", moduleNom,
                "volumeHoraire", vh.orElse(null),
                "trouve", vh.isPresent()
        ));
    }

    /** Obtenir le VH total d'un module sur plusieurs classes. */
    @PostMapping("/vh-total")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getVolumeHoraireTotal(@RequestBody Map<String, Object> body) {
        String moduleNom = (String) body.get("moduleNom");
        @SuppressWarnings("unchecked")
        List<String> classes = (List<String>) body.get("classes");
        double total = maquetteService.getVolumeHoraireTotal(moduleNom, classes);
        return ResponseEntity.ok(Map.of("volumeHoraireTotal", total));
    }

    /** Créer ou modifier une entrée maquette (upsert). */
    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<MaquetteModuleResponse> creerOuModifier(@Valid @RequestBody MaquetteModuleRequest request) {
        return ResponseEntity.ok(maquetteService.creerOuModifier(request));
    }

    /** Supprimer (désactiver) une entrée maquette. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        maquetteService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
