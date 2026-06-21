package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.ContratModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratModuleRepository extends JpaRepository<ContratModule, Long> {
    List<ContratModule> findByContratId(Long contratId);
    List<ContratModule> findByContratVacataireId(Long vacataireId);
    List<ContratModule> findByContratAnneeAcademiqueAndContratVacataireId(String anneeAcademique, Long vacataireId);
}
