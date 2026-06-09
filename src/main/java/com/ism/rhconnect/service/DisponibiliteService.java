package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.DisponibiliteRequest;
import com.ism.rhconnect.dto.response.DisponibiliteResponse;
import com.ism.rhconnect.entity.Disponibilite;
import com.ism.rhconnect.entity.Utilisateur;
import com.ism.rhconnect.entity.Vacataire;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.repository.DisponibiliteRepository;
import com.ism.rhconnect.repository.UtilisateurRepository;
import com.ism.rhconnect.repository.VacataireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisponibiliteService {

    private final DisponibiliteRepository disponibiliteRepository;
    private final VacataireRepository vacataireRepository;
    private final UtilisateurRepository utilisateurRepository;

    /** Vacataire déclare un créneau de disponibilité. */
    @Transactional
    public DisponibiliteResponse declarer(DisponibiliteRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Vacataire vacataire = vacataireRepository.findByUtilisateurId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier vacataire introuvable"));

        Disponibilite d = Disponibilite.builder()
                .vacataire(vacataire)
                .date(request.getDate())
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .build();
        return toResponse(disponibiliteRepository.save(d));
    }

    /** Vacataire consulte ses propres disponibilités. */
    @Transactional(readOnly = true)
    public List<DisponibiliteResponse> mesDisponibilites() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return disponibiliteRepository.findByVacataireUtilisateurId(u.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** RP consulte les disponibilités d'un vacataire. */
    @Transactional(readOnly = true)
    public List<DisponibiliteResponse> parVacataire(Long vacataireId) {
        return disponibiliteRepository.findByVacataireId(vacataireId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** RP confirme une disponibilité. */
    @Transactional
    public DisponibiliteResponse confirmer(Long id) {
        Disponibilite d = disponibiliteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilité introuvable : " + id));
        d.setStatut(Disponibilite.StatutDisponibilite.CONFIRMEE);
        return toResponse(disponibiliteRepository.save(d));
    }

    /** RP liste toutes les disponibilités. */
    @Transactional(readOnly = true)
    public List<DisponibiliteResponse> listerTous() {
        return disponibiliteRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private DisponibiliteResponse toResponse(Disponibilite d) {
        Utilisateur u = d.getVacataire().getUtilisateur();
        return DisponibiliteResponse.builder()
                .id(d.getId())
                .vacataireId(d.getVacataire().getId())
                .nomVacataire(u.getPrenom() + " " + u.getNom())
                .date(d.getDate())
                .heureDebut(d.getHeureDebut())
                .heureFin(d.getHeureFin())
                .statut(d.getStatut())
                .dateCreation(d.getDateCreation())
                .build();
    }
}
