package com.ism.rhconnect.controller;

import com.ism.rhconnect.entity.ClasseRef;
import com.ism.rhconnect.entity.ModuleRef;
import com.ism.rhconnect.entity.NiveauEnseignement;
import com.ism.rhconnect.repository.ClasseRefRepository;
import com.ism.rhconnect.repository.ModuleRefRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReferenceController {

    private final ClasseRefRepository classeRefRepository;
    private final ModuleRefRepository moduleRefRepository;

    /* ── Classes de référence ── */

    @GetMapping("/api/classes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClasseRef>> listerClasses(
            @RequestParam(required = false) NiveauEnseignement niveau) {
        List<ClasseRef> result = (niveau != null)
                ? classeRefRepository.findByNiveauAndActifTrueOrderByNomAsc(niveau)
                : classeRefRepository.findByActifTrueOrderByNiveauAscNomAsc();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/classes")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<?> creerClasse(@RequestBody Map<String, String> body) {
        String nom = body.getOrDefault("nom", "").trim();
        String filiere = body.getOrDefault("filiere", "").trim();
        String niveauStr = body.getOrDefault("niveau", "").trim().toUpperCase();
        if (nom.isEmpty() || niveauStr.isEmpty()) {
            return ResponseEntity.badRequest().body("Nom et niveau sont obligatoires.");
        }
        if (classeRefRepository.existsByNom(nom)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cette classe existe déjà.");
        }
        NiveauEnseignement niveau;
        try {
            niveau = NiveauEnseignement.valueOf(niveauStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Niveau invalide (LICENCE ou MASTER).");
        }
        ClasseRef classe = ClasseRef.builder().nom(nom).niveau(niveau)
                .filiere(filiere.isEmpty() ? null : filiere).actif(true).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(classeRefRepository.save(classe));
    }

    @DeleteMapping("/api/classes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> archiverClasse(@PathVariable Long id) {
        classeRefRepository.findById(id).ifPresent(c -> {
            c.setActif(false);
            classeRefRepository.save(c);
        });
        return ResponseEntity.noContent().build();
    }

    /* ── Modules de référence ── */

    @GetMapping("/api/modules-ref")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ModuleRef>> listerModules() {
        return ResponseEntity.ok(moduleRefRepository.findByActifTrueOrderByNomAsc());
    }

    @PostMapping("/api/modules-ref")
    @PreAuthorize("hasAnyRole('RESPONSABLE_PROGRAMME', 'ADMIN')")
    public ResponseEntity<?> creerModule(@RequestBody Map<String, String> body) {
        String nom = body.getOrDefault("nom", "").trim();
        if (nom.isEmpty()) {
            return ResponseEntity.badRequest().body("Le nom est obligatoire.");
        }
        if (moduleRefRepository.existsByNom(nom)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce module existe déjà.");
        }
        ModuleRef module = ModuleRef.builder().nom(nom).actif(true).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(moduleRefRepository.save(module));
    }

    @DeleteMapping("/api/modules-ref/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> archiverModule(@PathVariable Long id) {
        moduleRefRepository.findById(id).ifPresent(m -> {
            m.setActif(false);
            moduleRefRepository.save(m);
        });
        return ResponseEntity.noContent().build();
    }
}
