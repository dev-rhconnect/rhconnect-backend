package com.ism.rhconnect.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaiementRequest {

    @NotNull(message = "L'identifiant du relevé est obligatoire")
    private Long feuilleHeureId;

    @NotNull(message = "Le taux horaire est obligatoire")
    @Positive(message = "Le taux horaire doit être positif")
    private Double tauxHoraire;
}
