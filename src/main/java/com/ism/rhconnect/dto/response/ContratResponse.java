package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.Contrat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ContratResponse {
    private Long id;
    private Long vacataireId;
    private String nomVacataire;
    private String emailVacataire;
    private String module;
    private String classe;
    private Double volumeHorairePrevisionnel;
    private Double tauxHoraire;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean pdfGenere;
    private boolean estAvenant;
    private Long contratParentId;
    private Contrat.StatutContrat statut;
    private LocalDateTime dateCreation;
}
