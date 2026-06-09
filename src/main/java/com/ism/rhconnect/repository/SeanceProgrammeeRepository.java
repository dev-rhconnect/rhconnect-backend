package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.SeanceProgrammee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SeanceProgrammeeRepository extends JpaRepository<SeanceProgrammee, Long> {
    List<SeanceProgrammee> findByDateSeanceBetween(LocalDate debut, LocalDate fin);
    List<SeanceProgrammee> findByContratVacataireUtilisateurId(Long utilisateurId);
    List<SeanceProgrammee> findByStatut(SeanceProgrammee.StatutSeance statut);
    List<SeanceProgrammee> findByContratId(Long contratId);
}
