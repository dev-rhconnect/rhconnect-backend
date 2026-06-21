package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contrat_modules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContratModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id", nullable = false)
    private Contrat contrat;

    @Column(nullable = false)
    private String nomModule;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contrat_module_classes", joinColumns = @JoinColumn(name = "contrat_module_id"))
    @Column(name = "classe")
    @Builder.Default
    private List<String> classes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauEnseignement niveau;

    /** Si plusieurs classes : 1 seul émargement (tronc commun). */
    @Builder.Default
    private Boolean estTroncCommun = false;

    @Column(nullable = false)
    private Double volumeHorairePrevisionnel;

    @Builder.Default
    private Double heuresEffectuees = 0.0;

    @Builder.Default
    private Double heuresRestantes = 0.0;

    /** Date réelle de démarrage, saisie par le RP lors de l'activation du module. */
    private LocalDate dateDemarrage;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutModule statut = StatutModule.NON_COMMENCE;

    public enum StatutModule {
        NON_COMMENCE, EN_COURS, TERMINE
    }
}
