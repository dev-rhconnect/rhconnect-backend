package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.Contrat;
import com.ism.rhconnect.entity.ContratModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContratModuleRepository extends JpaRepository<ContratModule, Long> {
    List<ContratModule> findByContratId(Long contratId);
    List<ContratModule> findByContratVacataireId(Long vacataireId);
    List<ContratModule> findByContratAnneeAcademiqueAndContratVacataireId(String anneeAcademique, Long vacataireId);

    @Query("SELECT cm FROM ContratModule cm WHERE :classeNom MEMBER OF cm.classes " +
           "AND cm.contrat.statut = :statutContrat " +
           "AND cm.statut = :statutModule")
    List<ContratModule> findModulesActifsParClasse(
            @Param("classeNom") String classeNom,
            @Param("statutContrat") Contrat.StatutContrat statutContrat,
            @Param("statutModule") ContratModule.StatutModule statutModule);
}
