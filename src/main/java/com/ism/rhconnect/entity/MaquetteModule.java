package com.ism.rhconnect.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "maquette_modules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"classe_ref_id", "module_ref_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaquetteModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "classe_ref_id", nullable = false)
    private ClasseRef classeRef;

    @ManyToOne
    @JoinColumn(name = "module_ref_id", nullable = false)
    private ModuleRef moduleRef;

    /** Nombre d'heures prévu pour ce module dans cette classe (issu du programme officiel). */
    @Column(nullable = false)
    private Double volumeHoraire;

    @Builder.Default
    private boolean actif = true;
}
