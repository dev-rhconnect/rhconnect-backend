package com.ism.rhconnect.dto.request;

import com.ism.rhconnect.entity.SeanceProgrammee;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SeanceProgrammeeRequest {

    @NotNull
    private Long contratId;

    private Long disponibiliteId;

    @NotNull
    private LocalDate dateSeance;

    @NotNull
    private LocalTime heureDebut;

    @NotNull
    private LocalTime heureFin;

    private SeanceProgrammee.TypeSeance typeSeance;

    private String salle;
}
