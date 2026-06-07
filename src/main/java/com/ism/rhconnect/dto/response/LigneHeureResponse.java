package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.LigneHeure;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class LigneHeureResponse {
    private Long id;
    private Long feuilleHeureId;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Double duree;
    private String observation;
    private LigneHeure.Statut statut;
}
