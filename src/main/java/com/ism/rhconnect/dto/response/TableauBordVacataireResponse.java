package com.ism.rhconnect.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TableauBordVacataireResponse {
    private Long vacataireId;
    private String nomVacataire;
    private String anneeAcademiqueEnCours;
    private String moisEnCours;
    private Double heuresMoisEnCours;
    private Double totalHeuresAnnee;
    private Double totalHeuresRestantesAnnee;
    private List<ContratModuleResponse> modules;
}
