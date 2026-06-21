package com.ism.rhconnect.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AvenantRequest {

    @NotEmpty(message = "Au moins un module est obligatoire")
    @Valid
    private List<ContratModuleRequest> modules;

    /** Date de début de l'avenant (défaut : date du contrat parent). */
    private LocalDate dateDebut;
}
