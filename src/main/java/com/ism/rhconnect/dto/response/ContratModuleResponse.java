package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.ContratModule;
import com.ism.rhconnect.entity.NiveauEnseignement;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ContratModuleResponse {
    private Long id;
    private String nomModule;
    private List<String> classes;
    private NiveauEnseignement niveau;
    private Boolean estTroncCommun;
    private Double tauxHoraire;
    private Double volumeHorairePrevisionnel;
    private Double heuresEffectuees;
    private Double heuresRestantes;
    private LocalDate dateDemarrage;
    private ContratModule.StatutModule statut;
}
