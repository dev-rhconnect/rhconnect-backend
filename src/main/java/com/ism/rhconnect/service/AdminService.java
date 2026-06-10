package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.RegisterRequest;
import com.ism.rhconnect.dto.response.UtilisateurResponse;
import com.ism.rhconnect.entity.AuditLog;
import com.ism.rhconnect.entity.Utilisateur;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.repository.AuditLogRepository;
import com.ism.rhconnect.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerTous() {
        return utilisateurRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UtilisateurResponse creerCompte(RegisterRequest req) {
        if (req.getRole() == com.ism.rhconnect.entity.Role.VACATAIRE) {
            throw new IllegalArgumentException("L'Admin IT ne peut pas créer un compte vacataire. Cette action revient au Responsable de Programme.");
        }
        if (req.getRole() == com.ism.rhconnect.entity.Role.ADMIN) {
            throw new IllegalArgumentException("Il ne peut exister qu'un seul compte Admin IT dans le système.");
        }
        if (utilisateurRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé : " + req.getEmail());
        }

        String mdp = (req.getMotDePasseTemporaire() != null && !req.getMotDePasseTemporaire().isBlank())
                ? req.getMotDePasseTemporaire()
                : "Rhconnect@ISM2026";

        Utilisateur u = Utilisateur.builder()
                .prenom(req.getPrenom())
                .nom(req.getNom())
                .email(req.getEmail())
                .motDePasse(passwordEncoder.encode(mdp))
                .role(req.getRole())
                .actif(true)
                .build();

        Utilisateur saved = utilisateurRepository.save(u);
        journaliser("CREATE_USER", "Utilisateur", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void activer(Long id) {
        Utilisateur u = findOrThrow(id);
        u.setActif(true);
        utilisateurRepository.save(u);
        journaliser("ACTIVER_USER", "Utilisateur", id);
    }

    @Transactional
    public void desactiver(Long id) {
        Utilisateur u = findOrThrow(id);
        u.setActif(false);
        utilisateurRepository.save(u);
        journaliser("DESACTIVER_USER", "Utilisateur", id);
    }

    @Transactional
    public void reinitialiserMotDePasse(Long id, String nouveauMdp) {
        Utilisateur u = findOrThrow(id);
        String mdp = (nouveauMdp != null && !nouveauMdp.isBlank()) ? nouveauMdp : "Rhconnect@ISM2026";
        u.setMotDePasse(passwordEncoder.encode(mdp));
        u.setPremierConnexion(true);
        utilisateurRepository.save(u);
        journaliser("RESET_PASSWORD", "Utilisateur", id);
    }

    @Transactional
    public UtilisateurResponse changerRole(Long id, com.ism.rhconnect.entity.Role nouveauRole) {
        if (nouveauRole == com.ism.rhconnect.entity.Role.ADMIN) {
            throw new IllegalArgumentException("Il ne peut exister qu'un seul compte Admin IT dans le système.");
        }
        if (nouveauRole == com.ism.rhconnect.entity.Role.VACATAIRE) {
            throw new IllegalArgumentException("Le rôle Vacataire est attribué uniquement par le Responsable Pédagogique.");
        }
        Utilisateur u = findOrThrow(id);
        u.setRole(nouveauRole);
        Utilisateur saved = utilisateurRepository.save(u);
        journaliser("CHANGER_ROLE", "Utilisateur", id);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> listerLogs() {
        return auditLogRepository.findAll();
    }

    /* ── Helpers ── */

    private Utilisateur findOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    private UtilisateurResponse toResponse(Utilisateur u) {
        return UtilisateurResponse.builder()
                .id(u.getId())
                .prenom(u.getPrenom())
                .nom(u.getNom())
                .email(u.getEmail())
                .role(u.getRole())
                .actif(u.isActif())
                .dateCreation(u.getDateCreation())
                .build();
    }

    private void journaliser(String action, String ressource, Long ressourceId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        utilisateurRepository.findByEmail(email).ifPresent(admin ->
            auditLogRepository.save(AuditLog.builder()
                    .utilisateur(admin)
                    .action(action)
                    .ressource(ressource)
                    .ressourceId(ressourceId)
                    .build()));
    }
}
