package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "modules_ref")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModuleRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;
}
