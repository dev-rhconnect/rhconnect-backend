package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.SeanceProgrammee;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class SeanceProgrammeeResponse {
    private Long id;
    private Long contratId;
    private String nomVacataire;
    private String emailVacataire;
    private String specialiteVacataire;
    private String module;
    private String classe;
    private LocalDate dateSeance;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Double duree;
    private SeanceProgrammee.TypeSeance typeSeance;
    private String salle;
    private SeanceProgrammee.StatutSeance statut;
    private boolean feuillePresenceUploaded;
    private String noteInterne;
    private String nomValidePar;
    private LocalDateTime dateValidation;
    private LocalDateTime dateCreation;
}
