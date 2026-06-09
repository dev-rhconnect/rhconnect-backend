package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "seances_programmees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeanceProgrammee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contrat_id", nullable = false)
    private Contrat contrat;

    @ManyToOne
    @JoinColumn(name = "disponibilite_id")
    private Disponibilite disponibilite;

    @Column(nullable = false)
    private LocalDate dateSeance;

    @Column(nullable = false)
    private LocalTime heureDebut;

    @Column(nullable = false)
    private LocalTime heureFin;

    @Column(nullable = false)
    private Double duree;

    @Enumerated(EnumType.STRING)
    private TypeSeance typeSeance;

    private String salle;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutSeance statut = StatutSeance.PROGRAMMEE;

    private String feuillePresencePath;
    private String noteInterne;

    @ManyToOne
    @JoinColumn(name = "valide_par_id")
    private Utilisateur validePar;

    private LocalDateTime dateValidation;

    @ManyToOne
    @JoinColumn(name = "cree_par_id")
    private Utilisateur creePar;

    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    public enum TypeSeance {
        CM, TD, TP, CONFERENCE
    }

    public enum StatutSeance {
        PROGRAMMEE, REALISEE, ANNULEE
    }
}
