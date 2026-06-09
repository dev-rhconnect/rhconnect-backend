package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.Disponibilite;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class DisponibiliteResponse {
    private Long id;
    private Long vacataireId;
    private String nomVacataire;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Disponibilite.StatutDisponibilite statut;
    private LocalDateTime dateCreation;
}
