package com.ism.rhconnect.controller;

import com.ism.rhconnect.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;

    /**
     * RP — rapport mensuel PDF : liste des vacataires, heures effectuées vs prévues.
     * GET /api/rapports/rp/mensuel?mois=2026-06
     */
    @GetMapping("/rp/mensuel")
    @PreAuthorize("hasRole('RESPONSABLE_PROGRAMME')")
    public ResponseEntity<byte[]> rapportRpMensuel(@RequestParam String mois) throws Exception {
        byte[] pdf = rapportService.rapportRpMensuelPdf(mois);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport-rp-" + mois + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Finance — rapport paiements.
     * GET /api/rapports/finance/paiements?mois=2026-06&format=pdf   (défaut)
     * GET /api/rapports/finance/paiements?mois=2026-06&format=excel
     */
    @GetMapping("/finance/paiements")
    @PreAuthorize("hasRole('RELAIS_FINANCE')")
    public ResponseEntity<byte[]> rapportFinancePaiements(
            @RequestParam String mois,
            @RequestParam(defaultValue = "pdf") String format) throws Exception {

        if ("excel".equalsIgnoreCase(format)) {
            byte[] xlsx = rapportService.rapportFinanceExcel(mois);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"paiements-" + mois + ".xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(xlsx);
        }

        byte[] pdf = rapportService.rapportFinancePdf(mois);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"paiements-" + mois + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
