package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.LigneHeure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface LigneHeureRepository extends JpaRepository<LigneHeure, Long> {
    List<LigneHeure> findByFeuilleHeureId(Long feuilleHeureId);

    @Query("SELECT COUNT(l) > 0 FROM LigneHeure l WHERE l.feuilleHeure.contrat.id = :contratId AND l.date = :date")
    boolean existsDoublon(@Param("contratId") Long contratId, @Param("date") LocalDate date);

    @Query("SELECT SUM(l.duree) FROM LigneHeure l WHERE l.feuilleHeure.id = :feuilleHeureId AND l.statut = 'VALIDEE'")
    Double sumDureeValidee(@Param("feuilleHeureId") Long feuilleHeureId);
}
