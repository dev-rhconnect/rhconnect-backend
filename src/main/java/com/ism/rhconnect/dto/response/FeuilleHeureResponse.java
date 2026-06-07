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
    private String nomVacataire;
    private String module;
    private String classe;
    private String periode;
    private Double totalHeuresValidees;
    private FeuilleHeure.Statut statut;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateValidation;
    private List<LigneHeureResponse> lignes;
}
