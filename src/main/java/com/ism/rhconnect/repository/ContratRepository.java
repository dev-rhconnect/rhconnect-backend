package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContratRepository extends JpaRepository<Contrat, Long> {
    List<Contrat> findByVacataireId(Long vacataireId);
    List<Contrat> findByStatut(Contrat.StatutContrat statut);
    Optional<Contrat> findByVacataireIdAndStatut(Long vacataireId, Contrat.StatutContrat statut);
    boolean existsByVacataireIdAndStatut(Long vacataireId, Contrat.StatutContrat statut);

    @Query("SELECT c FROM Contrat c WHERE c.dateFin BETWEEN :debut AND :fin AND c.statut = 'ACTIF'")
    List<Contrat> findContratsExpirantEntre(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
