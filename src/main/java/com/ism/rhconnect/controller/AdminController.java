package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.RegisterRequest;
import com.ism.rhconnect.dto.response.UtilisateurResponse;
import com.ism.rhconnect.entity.AuditLog;
import com.ism.rhconnect.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sprint 2 — Marie DIAGNE
 * Administration : gestion des comptes utilisateurs et consultation des logs d'audit.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /** Lister tous les utilisateurs. */
    @GetMapping("/utilisateurs")
    public ResponseEntity<List<UtilisateurResponse>> listerUtilisateurs() {
        return ResponseEntity.ok(adminService.listerTous());
    }

    /** Créer un nouveau compte utilisateur. */
    @PostMapping("/utilisateurs")
    public ResponseEntity<UtilisateurResponse> creerCompte(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.creerCompte(request));
    }

    /** Activer un compte. */
    @PatchMapping("/utilisateurs/{id}/activer")
    public ResponseEntity<Void> activer(@PathVariable Long id) {
        adminService.activer(id);
        return ResponseEntity.noContent().build();
    }

    /** Désactiver un compte. */
    @PatchMapping("/utilisateurs/{id}/desactiver")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        adminService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    /** Changer le rôle d'un compte. */
    @PatchMapping("/utilisateurs/{id}/role")
    public ResponseEntity<UtilisateurResponse> changerRole(
            @PathVariable Long id,
            @RequestParam String role) {
        return ResponseEntity.ok(adminService.changerRole(id, com.ism.rhconnect.entity.Role.valueOf(role)));
    }

    /** Consulter les logs d'audit. */
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> logs() {
        return ResponseEntity.ok(adminService.listerLogs());
    }
}
