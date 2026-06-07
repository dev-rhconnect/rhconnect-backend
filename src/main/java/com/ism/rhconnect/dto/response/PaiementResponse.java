package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.Paiement;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaiementResponse {
    private Long id;
    private Long feuilleHeureId;
    private String nomVacataire;
    private String periode;
    private Double totalHeures;
    private Double tauxHoraire;
    private Double montantBrut;
    private Double retenueFiscale;
    private Double montantNet;
    private Paiement.Statut statut;
    private LocalDateTime dateGeneration;
}
