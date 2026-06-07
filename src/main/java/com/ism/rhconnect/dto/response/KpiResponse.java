package com.ism.rhconnect.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KpiResponse {
    private long vacatairesActifs;
    private long vacatairesTotal;
    private double totalHeuresValidees;
    private double totalRemunerationsNet;
    private double totalRemunerationsBrut;
    private long relevesEnCours;
    private long relevesSoumis;
    private long contratsActifs;
    private long contratsExpirantBientot;
    private List<StatMensuelle> evolutionMensuelle;

    @Data
    @Builder
    public static class StatMensuelle {
        private String mois;
        private double heures;
        private double montantNet;
    }
}
