package com.ism.rhconnect.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FicheDecompteRequest {

    @NotNull
    private Long vacataireId;

    @NotNull
    private Long contratModuleId;
}
