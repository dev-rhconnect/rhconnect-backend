package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.FeuilleHeure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FeuilleHeureRepository extends JpaRepository<FeuilleHeure, Long> {
    List<FeuilleHeure> findByContratId(Long contratId);
    List<FeuilleHeure> findByStatut(FeuilleHeure.Statut statut);
    long countByStatut(FeuilleHeure.Statut statut);
    List<FeuilleHeure> findByAttacheId(Long attacheId);
    Optional<FeuilleHeure> findByContratIdAndPeriode(Long contratId, String periode);
}
