package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.response.KpiResponse;
import com.ism.rhconnect.service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sprint 2 — Diaynaba SOW
 * Tableau de bord : KPIs agrégés pour les graphiques Recharts.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final KpiService kpiService;

    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_PROGRAMME', 'RELAIS_FINANCE')")
    public ResponseEntity<KpiResponse> kpis() {
        return ResponseEntity.ok(kpiService.calculer());
    }
}
