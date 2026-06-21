package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(name = "fiches_decompte")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FicheDecompte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacataire_id", nullable = false)
    private Vacataire vacataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_module_id", nullable = false)
    private ContratModule contratModule;

    private YearMonth mois;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutFiche statut = StatutFiche.EN_COURS;

    private Double dureeTotal;
    private Double montantBrut;

    @Column(nullable = false)
    @Builder.Default
    private boolean valideeParAttache = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean valideeParRelaisFinance = false;

    private LocalDate dateCompilation;
    private LocalDate dateValidation;

    private LocalDate dateCreation;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) dateCreation = LocalDate.now();
        if (mois == null) mois = YearMonth.now();
    }

    public enum StatutFiche {
        EN_COURS,           // Séances en cours de relevé
        COMPILEE,           // Fiche compilée, prête pour validation
        VALIDEE_ATTACHE,    // Validée par attaché
        VALIDEE_FINANCE,    // Validée par relais finance
        PAYEE               // Paiement effectué
    }
}
