package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.FicheDecompte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface FicheDecompteRepository extends JpaRepository<FicheDecompte, Long> {
    List<FicheDecompte> findByVacataireIdAndMois(Long vacataireId, YearMonth mois);
    List<FicheDecompte> findByStatut(FicheDecompte.StatutFiche statut);
    List<FicheDecompte> findByMois(YearMonth mois);
    Optional<FicheDecompte> findByVacataireIdAndContratModuleIdAndMois(
            Long vacataireId, Long contratModuleId, YearMonth mois);
}
