package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contrats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vacataire_id", nullable = false)
    private Vacataire vacataire;

    @Column(nullable = false)
    private String module;

    @Column(nullable = false)
    private String classe;

    @Column(nullable = false)
    private Double volumeHorairePrevisionnel;

    @Column(nullable = false)
    private Double tauxHoraire;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    private String cheminPdf;

    @Builder.Default
    private boolean estAvenant = false;

    @ManyToOne
    @JoinColumn(name = "contrat_parent_id")
    private Contrat contratParent;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutContrat statut = StatutContrat.ACTIF;

    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    public enum StatutContrat {
        ACTIF, EXPIRE, RESILIE
    }
}
