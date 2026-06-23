package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.FeuilleHeure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeuilleHeureRepository extends JpaRepository<FeuilleHeure, Long> {

    List<FeuilleHeure> findByContratId(Long contratId);
    List<FeuilleHeure> findByStatut(FeuilleHeure.Statut statut);
    List<FeuilleHeure> findByStatutIn(List<FeuilleHeure.Statut> statuts);
    long countByStatut(FeuilleHeure.Statut statut);
    List<FeuilleHeure> findByAttacheId(Long attacheId);
    Optional<FeuilleHeure> findByContratIdAndPeriode(Long contratId, String periode);

    /** Trouve le relevé EN_COURS pour un ContratModule + mois donné. */
    Optional<FeuilleHeure> findByContratModuleIdAndPeriode(Long contratModuleId, String periode);

    /** Trouve le relevé EN_COURS pour un Contrat (sans ContratModule) + mois donné. */
    Optional<FeuilleHeure> findByContratIdAndPeriodeAndContratModuleIsNull(Long contratId, String periode);

    List<FeuilleHeure> findByContratVacataireUtilisateurIdAndStatut(Long utilisateurId, FeuilleHeure.Statut statut);
    List<FeuilleHeure> findByContratVacataireUtilisateurId(Long utilisateurId);

    @Query("SELECT f FROM FeuilleHeure f WHERE f.attache.id = :attacheId AND (:periode IS NULL OR f.periode = :periode)")
    List<FeuilleHeure> findByAttacheIdAndPeriodeOpt(@Param("attacheId") Long attacheId,
                                                     @Param("periode") String periode);
}
