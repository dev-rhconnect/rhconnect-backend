package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private String module;

    private String classe;

    private Double volumeHorairePrevisionnel;

    private Double tauxHoraire;

    private String anneeAcademique;

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

    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ContratModule> modules = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    public enum StatutContrat {
        ACTIF, EXPIRE, RESILIE
    }
}
