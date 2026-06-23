package com.ism.rhconnect.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ContratRequest {

    @NotNull(message = "L'identifiant du vacataire est obligatoire")
    private Long vacataireId;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    @NotNull(message = "Le taux horaire est obligatoire")
    @Positive(message = "Le taux horaire doit être positif")
    private Double tauxHoraire;

    @NotEmpty(message = "Au moins un module est obligatoire")
    @Valid
    private List<ContratModuleRequest> modules;

    /** Pour un avenant : identifiant du contrat d'origine */
    private Long contratParentId;

    private boolean estAvenant;

    /** Ex : "2025-2026" — calculé automatiquement si absent */
    private String anneeAcademique;
}
