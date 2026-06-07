package com.ism.rhconnect.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class LigneHeureRequest {

    @NotNull(message = "L'identifiant de la feuille d'heures est obligatoire")
    private Long feuilleHeureId;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime heureFin;

    private String observation;
}
