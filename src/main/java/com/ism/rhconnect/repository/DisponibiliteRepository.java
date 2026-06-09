package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.Disponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {
    List<Disponibilite> findByVacataireId(Long vacataireId);
    List<Disponibilite> findByVacataireUtilisateurId(Long utilisateurId);
}
