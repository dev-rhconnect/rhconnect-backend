package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.ClasseRef;
import com.ism.rhconnect.entity.NiveauEnseignement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClasseRefRepository extends JpaRepository<ClasseRef, Long> {
    List<ClasseRef> findByActifTrueOrderByNiveauAscNomAsc();
    List<ClasseRef> findByNiveauAndActifTrueOrderByNomAsc(NiveauEnseignement niveau);
    boolean existsByNom(String nom);
    Optional<ClasseRef> findByNom(String nom);
}
