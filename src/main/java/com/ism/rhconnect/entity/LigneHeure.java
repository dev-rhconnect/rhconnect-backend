package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "lignes_heures")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LigneHeure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "feuille_heure_id", nullable = false)
    private FeuilleHeure feuilleHeure;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime heureDebut;

    @Column(nullable = false)
    private LocalTime heureFin;

    @Column(nullable = false)
    private Double duree;

    private String observation;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Statut statut = Statut.SAISIE;

    public enum Statut {
        SAISIE, VALIDEE, REJETEE
    }
}
