package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.LigneHeureRequest;
import com.ism.rhconnect.dto.response.LigneHeureResponse;
import com.ism.rhconnect.entity.FeuilleHeure;
import com.ism.rhconnect.entity.LigneHeure;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.repository.FeuilleHeureRepository;
import com.ism.rhconnect.repository.LigneHeureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterventionService {

    private final FeuilleHeureRepository feuilleHeureRepository;
    private final LigneHeureRepository ligneHeureRepository;

    @Transactional
    public LigneHeureResponse ajouterLigne(LigneHeureRequest request) {
        if (!request.getHeureFin().isAfter(request.getHeureDebut())) {
            throw new IllegalArgumentException("L'heure de fin doit être postérieure à l'heure de début");
        }

        FeuilleHeure feuille = feuilleHeureRepository.findById(request.getFeuilleHeureId())
                .orElseThrow(() -> new ResourceNotFoundException("Feuille d'heures introuvable : " + request.getFeuilleHeureId()));

        if (feuille.getStatut() != FeuilleHeure.Statut.EN_COURS) {
            throw new IllegalStateException("Impossible d'ajouter une séance à un relevé " + feuille.getStatut().name().toLowerCase());
        }

        double duree = Duration.between(request.getHeureDebut(), request.getHeureFin()).toMinutes() / 60.0;

        LigneHeure ligne = LigneHeure.builder()
                .feuilleHeure(feuille)
                .date(request.getDate())
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .duree(duree)
                .observation(request.getObservation())
                .build();

        return toResponse(ligneHeureRepository.save(ligne));
    }

    @Transactional(readOnly = true)
    public List<LigneHeureResponse> listerParFeuille(Long feuilleHeureId) {
        FeuilleHeure feuille = feuilleHeureRepository.findById(feuilleHeureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feuille d'heures introuvable : " + feuilleHeureId));
        return feuille.getLignes().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private LigneHeureResponse toResponse(LigneHeure l) {
        return LigneHeureResponse.builder()
                .id(l.getId())
                .feuilleHeureId(l.getFeuilleHeure().getId())
                .date(l.getDate())
                .heureDebut(l.getHeureDebut())
                .heureFin(l.getHeureFin())
                .duree(l.getDuree())
                .observation(l.getObservation())
                .statut(l.getStatut())
                .build();
    }
}
