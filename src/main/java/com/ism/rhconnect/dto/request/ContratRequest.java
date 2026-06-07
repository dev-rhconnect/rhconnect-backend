package com.ism.rhconnect.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ContratRequest {

    @NotNull(message = "L'identifiant du vacataire est obligatoire")
    private Long vacataireId;

    @NotBlank(message = "Le module est obligatoire")
    private String module;

    @NotBlank(message = "La classe est obligatoire")
    private String classe;

    @NotNull(message = "Le volume horaire prévisionnel est obligatoire")
    @Positive(message = "Le volume horaire doit être positif")
    private Double volumeHorairePrevisionnel;

    @NotNull(message = "Le taux horaire est obligatoire")
    @Positive(message = "Le taux horaire doit être positif")
    private Double tauxHoraire;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    /** Pour un avenant : identifiant du contrat d'origine */
    private Long contratParentId;

    private boolean estAvenant;
}
