package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "classes_ref")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClasseRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauEnseignement niveau;

    private String filiere;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;
}
