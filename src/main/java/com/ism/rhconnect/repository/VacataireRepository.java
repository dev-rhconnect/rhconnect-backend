package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.Vacataire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VacataireRepository extends JpaRepository<Vacataire, Long> {
    Optional<Vacataire> findByUtilisateurId(Long utilisateurId);
    List<Vacataire> findByStatut(Vacataire.StatutVacataire statut);
    long countByStatut(Vacataire.StatutVacataire statut);
    boolean existsByUtilisateurEmail(String email);
}
