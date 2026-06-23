package com.ism.rhconnect.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ModuleActifParClasseResponse {
    private Long contratModuleId;
    private Long contratId;
    private String nomModule;
    private String niveau;
    private List<String> classes;
    private Double volumeHorairePrevisionnel;
    private Double heuresEffectuees;
    private String vacataireNom;
    private Long vacataireId;
}
