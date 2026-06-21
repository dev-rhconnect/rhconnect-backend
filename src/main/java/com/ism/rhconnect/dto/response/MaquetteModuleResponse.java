package com.ism.rhconnect.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaquetteModuleResponse {
    private Long id;
    private String classeNom;
    private String classeFiliere;
    private String moduleNom;
    private Double volumeHoraire;
    private boolean actif;
}
