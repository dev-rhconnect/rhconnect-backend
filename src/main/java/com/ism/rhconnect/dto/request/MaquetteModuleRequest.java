package com.ism.rhconnect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MaquetteModuleRequest {

    @NotBlank(message = "Le nom de la classe est obligatoire")
    private String classeNom;

    @NotBlank(message = "Le nom du module est obligatoire")
    private String moduleNom;

    @NotNull(message = "Le volume horaire est obligatoire")
    @Positive(message = "Le volume horaire doit être positif")
    private Double volumeHoraire;
}
