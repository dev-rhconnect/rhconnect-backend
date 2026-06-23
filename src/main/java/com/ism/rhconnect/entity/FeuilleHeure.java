package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "feuilles_heures")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeuilleHeure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id", nullable = false)
    private Contrat contrat;

    /** Module concerné par ce relevé (null pour anciens contrats mono-module). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_module_id")
    private ContratModule contratModule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attache_id", nullable = false)
    private Utilisateur attache;

    @Column(nullable = false)
    private String periode;

    @Builder.Default
    private Double totalHeuresValidees = 0.0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Statut statut = Statut.EN_COURS;

    private LocalDateTime dateSoumission;
    private LocalDateTime dateValidation;
    private String motifRejet;

    @OneToMany(mappedBy = "feuilleHeure", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LigneHeure> lignes;

    public enum Statut {
        EN_COURS,
        SOUMIS,          // legacy (= SOUMIS_RP)
        SOUMIS_RP,       // attaché a soumis au RP
        VALIDE_RP,       // RP a validé
        SOUMIS_FINANCE,  // RP a transmis au relais finance
        VALIDE,          // Finance a validé
        REJETE
    }
}
