package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.Utilisateur;
import com.ism.rhconnect.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Utilisateur> findByRole(Role role);
    List<Utilisateur> findByActif(boolean actif);
}
