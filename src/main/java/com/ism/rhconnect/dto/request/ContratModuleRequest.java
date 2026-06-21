package com.ism.rhconnect.dto.request;

import com.ism.rhconnect.entity.NiveauEnseignement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ContratModuleRequest {

    @NotBlank(message = "Le nom du module est obligatoire")
    private String nomModule;

    @NotEmpty(message = "Au moins une classe est obligatoire")
    private List<String> classes;

    @NotNull(message = "Le niveau est obligatoire")
    private NiveauEnseignement niveau;

    /** Si plusieurs classes : cocher "Tronc commun ?" → 1 seul émargement. */
    private boolean estTroncCommun;

    // volumeHorairePrevisionnel n'est plus saisi manuellement :
    // il est déduit automatiquement de la maquette (classe + module)
}
