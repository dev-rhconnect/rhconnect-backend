package com.ism.rhconnect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeuilleHeureRequest {

    @NotNull(message = "L'identifiant du contrat est obligatoire")
    private Long contratId;

    @NotBlank(message = "La période est obligatoire (ex: Mai 2026)")
    private String periode;
}
