package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String ressource;

    private Long ressourceId;

    private String adresseIp;

    private LocalDateTime dateAction;

    @PrePersist
    protected void onCreate() {
        dateAction = LocalDateTime.now();
    }
}
