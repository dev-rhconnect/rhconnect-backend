package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "feuille_heure_id", nullable = false)
    private FeuilleHeure feuilleHeure;

    @Column(nullable = false)
    private Double totalHeures;

    @Column(nullable = false)
    private Double tauxHoraire;

    @Column(nullable = false)
    private Double montantBrut;

    // Retenue fiscale 5% CGI art. 200
    @Column(nullable = false)
    private Double retenueFiscale;

    @Column(nullable = false)
    private Double montantNet;

    private String cheminFichePaie;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Statut statut = Statut.EN_ATTENTE;

    private LocalDateTime dateGeneration;
    private LocalDateTime dateValidation;

    @ManyToOne
    @JoinColumn(name = "valide_par_id")
    private Utilisateur validePar;

    @PrePersist
    protected void onCreate() {
        dateGeneration = LocalDateTime.now();
        // Calcul automatique
        montantBrut    = totalHeures * tauxHoraire;
        retenueFiscale = montantBrut * 0.05;
        montantNet     = montantBrut - retenueFiscale;
    }

    public enum Statut {
        EN_ATTENTE, VALIDE, PAYE
    }
}
