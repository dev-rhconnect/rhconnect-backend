package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByFeuilleHeureContratVacataireId(Long vacataireId);
    List<Paiement> findByStatut(Paiement.Statut statut);

    @Query("SELECT SUM(p.montantNet) FROM Paiement p WHERE p.feuilleHeure.periode = :periode AND p.statut = 'VALIDE'")
    Double sumMontantNetByPeriode(@Param("periode") String periode);
}
