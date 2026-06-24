package com.ism.rhconnect.entity;

import com.ism.rhconnect.entity.TypeVacataire;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "vacataires")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vacataire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    private String specialite;

    private String telephone;
    private String adresse;
    private String numeroCni;
    private String ninea;
    private String ipres;
    private String situationMatrimoniale;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String nationalite;

    // Coordonnées bancaires
    private String nomBanque;
    private String codeBanque;
    private String codeGuichet;
    private String numeroCompte;
    private String rib;

    // Signature électronique
    private String cheminSignature;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TypeVacataire typeVacataire = TypeVacataire.STANDARD;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutVacataire statut = StatutVacataire.ACTIF;

    public enum StatutVacataire {
        ACTIF, INACTIF, SUSPENDU
    }
}
