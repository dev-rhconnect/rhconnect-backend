package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Canal canal = Canal.IN_APP;

    @Builder.Default
    private boolean lu = false;

    private LocalDateTime dateEnvoi;

    @PrePersist
    protected void onCreate() {
        dateEnvoi = LocalDateTime.now();
    }

    public enum Type {
        CONTRAT_EXPIRANT,
        RELEVE_SOUMIS,
        RELEVE_VALIDE,
        RELEVE_REJETE,
        FICHE_PAIE_DISPONIBLE,
        ECART_VOLUME_HORAIRE,
        NOUVEAU_COMPTE,
        DEMANDE_EXPLICATION
    }

    public enum Canal {
        IN_APP, EMAIL, LES_DEUX
    }
}
