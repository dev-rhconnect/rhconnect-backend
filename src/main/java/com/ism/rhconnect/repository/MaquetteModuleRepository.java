package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.MaquetteModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaquetteModuleRepository extends JpaRepository<MaquetteModule, Long> {

    List<MaquetteModule> findByActifTrue();

    List<MaquetteModule> findByClasseRefNomAndActifTrue(String classeRefNom);

    Optional<MaquetteModule> findByClasseRefNomAndModuleRefNom(String classeNom, String moduleNom);

    @Query("SELECT m FROM MaquetteModule m WHERE m.moduleRef.nom = :moduleNom AND m.actif = true")
    List<MaquetteModule> findByModuleNom(@Param("moduleNom") String moduleNom);

    boolean existsByClasseRefIdAndModuleRefId(Long classeRefId, Long moduleRefId);
}
