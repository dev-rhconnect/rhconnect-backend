package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.FeuilleHeure;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FeuilleHeureResponse {
    private Long id;
    private Long contratId;
    private Long contratModuleId;
    private Long vacataireId;
    private String nomVacataire;
    private String module;
    private String classe;
    private List<String> classes;
    private String periode;
    private int nombreSeances;
    private Double totalHeuresValidees;
    private Double volumeHorairePrevisionnel;
    private Double tauxHoraire;
    private FeuilleHeure.Statut statut;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateValidation;
    private String motifRejet;
    private List<LigneHeureResponse> lignes;
}
