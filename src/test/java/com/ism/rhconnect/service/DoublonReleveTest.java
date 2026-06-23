package com.ism.rhconnect.service;

import com.ism.rhconnect.entity.*;
import com.ism.rhconnect.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auto-création et anti-doublon de relevé via creerOuIncrementer")
class DoublonReleveTest {

    @InjectMocks  ReleveService            service;
    @Mock FeuilleHeureRepository           feuilleHeureRepository;
    @Mock ContratRepository                contratRepository;
    @Mock UtilisateurRepository            utilisateurRepository;
    @Mock LigneHeureRepository             ligneHeureRepository;
    @Mock NotificationService              notificationService;
    @Mock PaiementRepository               paiementRepository;
    @Mock EmailService                     emailService;
    @Mock SeanceProgrammeeRepository       seanceProgrammeeRepository;

    private Utilisateur attache;
    private Contrat contrat;
    private SeanceProgrammee seance;

    @BeforeEach
    void setup() {
        attache = Utilisateur.builder().id(1L).email("attache@ism.edu.sn").build();

        Utilisateur vacataireUser = Utilisateur.builder()
                .id(2L).prenom("Mansour").nom("Diallo").email("mansour@ism.edu.sn").build();
        Vacataire vacataire = Vacataire.builder().id(1L).utilisateur(vacataireUser).build();

        contrat = Contrat.builder()
                .id(10L).statut(Contrat.StatutContrat.ACTIF)
                .module("Business English 1").classe("L3-GLRS")
                .vacataire(vacataire).build();

        seance = SeanceProgrammee.builder()
                .id(42L)
                .contrat(contrat)
                .contratModule(null)
                .dateSeance(LocalDate.of(2026, 6, 15))
                .heureDebut(LocalTime.of(8, 0))
                .heureFin(LocalTime.of(10, 0))
                .duree(2.0)
                .build();
    }

    @Test
    @DisplayName("Première séance du mois → nouveau relevé créé + ligne ajoutée")
    void premiere_seance_cree_releve() {
        FeuilleHeure nouvelleFeuille = FeuilleHeure.builder()
                .id(1L).contrat(contrat).periode("2026-06")
                .statut(FeuilleHeure.Statut.EN_COURS).build();

        when(feuilleHeureRepository.findByContratIdAndPeriodeAndContratModuleIsNull(10L, "2026-06"))
                .thenReturn(Optional.empty());
        when(feuilleHeureRepository.save(any())).thenReturn(nouvelleFeuille);
        when(ligneHeureRepository.existsByFeuilleHeureIdAndSeanceProgrammeeId(1L, 42L))
                .thenReturn(false);

        service.creerOuIncrementer(seance, attache);

        verify(feuilleHeureRepository, atLeastOnce()).save(any());
        verify(ligneHeureRepository).save(any(LigneHeure.class));
    }

    @Test
    @DisplayName("Relevé existant EN_COURS → ligne ajoutée, pas de nouveau relevé")
    void releve_existant_incremente() {
        FeuilleHeure existante = FeuilleHeure.builder()
                .id(5L).contrat(contrat).periode("2026-06")
                .statut(FeuilleHeure.Statut.EN_COURS).build();

        when(feuilleHeureRepository.findByContratIdAndPeriodeAndContratModuleIsNull(10L, "2026-06"))
                .thenReturn(Optional.of(existante));
        when(ligneHeureRepository.existsByFeuilleHeureIdAndSeanceProgrammeeId(5L, 42L))
                .thenReturn(false);

        service.creerOuIncrementer(seance, attache);

        // Pas de création de nouvelle feuille
        verify(feuilleHeureRepository, never()).save(argThat(f -> ((FeuilleHeure) f).getId() == null));
        verify(ligneHeureRepository).save(any(LigneHeure.class));
    }

    @Test
    @DisplayName("Doublon détecté → ligne NON ajoutée une seconde fois")
    void doublon_ligne_ignoree() {
        FeuilleHeure existante = FeuilleHeure.builder()
                .id(5L).contrat(contrat).periode("2026-06")
                .statut(FeuilleHeure.Statut.EN_COURS).build();

        when(feuilleHeureRepository.findByContratIdAndPeriodeAndContratModuleIsNull(10L, "2026-06"))
                .thenReturn(Optional.of(existante));
        when(ligneHeureRepository.existsByFeuilleHeureIdAndSeanceProgrammeeId(5L, 42L))
                .thenReturn(true); // déjà présente

        service.creerOuIncrementer(seance, attache);

        verify(ligneHeureRepository, never()).save(any());
    }

    @Test
    @DisplayName("Relevé déjà SOUMIS_RP → ligne NON ajoutée")
    void releve_soumis_non_modifie() {
        FeuilleHeure soumise = FeuilleHeure.builder()
                .id(7L).contrat(contrat).periode("2026-06")
                .statut(FeuilleHeure.Statut.SOUMIS_RP).build();

        when(feuilleHeureRepository.findByContratIdAndPeriodeAndContratModuleIsNull(10L, "2026-06"))
                .thenReturn(Optional.of(soumise));

        service.creerOuIncrementer(seance, attache);

        verify(ligneHeureRepository, never()).save(any());
    }
}
