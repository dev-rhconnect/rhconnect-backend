package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.FicheDecompte;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.YearMonth;

@Data
@Builder
public class FicheDecompteResponse {
    private Long id;
    private Long vacataireId;
    private String nomVacataire;
    private String emailVacataire;
    private Long contratModuleId;
    private String nomModule;
    private String classes;
    private String niveau;
    private YearMonth mois;
    private FicheDecompte.StatutFiche statut;
    private Double dureeTotal;
    private Double montantBrut;
    private Double tauxHoraire;
    private boolean valideeParAttache;
    private boolean valideeParRelaisFinance;
    private LocalDate dateCompilation;
    private LocalDate dateValidation;
    private LocalDate dateCreation;
}
