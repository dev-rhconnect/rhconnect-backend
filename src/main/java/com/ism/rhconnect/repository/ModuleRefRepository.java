package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.ModuleRef;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ModuleRefRepository extends JpaRepository<ModuleRef, Long> {
    List<ModuleRef> findByActifTrueOrderByNomAsc();
    boolean existsByNom(String nom);
    Optional<ModuleRef> findByNom(String nom);
}
