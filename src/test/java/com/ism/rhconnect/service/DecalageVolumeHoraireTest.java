package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.LigneHeureRequest;
import com.ism.rhconnect.entity.*;
import com.ism.rhconnect.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Détection de décalage Volume Horaire — dépassement du VH prévisionnel")
class DecalageVolumeHoraireTest {

    @InjectMocks  ReleveService            service;
    @Mock FeuilleHeureRepository           feuilleHeureRepository;
    @Mock ContratRepository                contratRepository;
    @Mock UtilisateurRepository            utilisateurRepository;
    @Mock LigneHeureRepository             ligneHeureRepository;
    @Mock NotificationService              notificationService;
    @Mock PaiementRepository               paiementRepository;
    @Mock EmailService                     emailService;
    @Mock SeanceProgrammeeRepository       seanceProgrammeeRepository;

    @BeforeEach
    void setupSecurity() {
        var auth = new UsernamePasswordAuthenticationToken(
                "attache@ism.edu.sn", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private FeuilleHeure buildFeuille(double volumePrevisionnel, double heuresDejaDeclarees) {
        Contrat contrat = Contrat.builder()
                .id(1L).statut(Contrat.StatutContrat.ACTIF)
                .volumeHorairePrevisionnel(volumePrevisionnel)
                .module("Business English 1").classe("L3-GLRS")
                .build();

        LigneHeure ligneExistante = mock(LigneHeure.class);
        when(ligneExistante.getDuree()).thenReturn(heuresDejaDeclarees);
        when(ligneExistante.getStatut()).thenReturn(LigneHeure.Statut.SAISIE);

        FeuilleHeure feuille = mock(FeuilleHeure.class);
        when(feuille.getStatut()).thenReturn(FeuilleHeure.Statut.EN_COURS);
        when(feuille.getContrat()).thenReturn(contrat);
        when(feuille.getLignes()).thenReturn(List.of(ligneExistante));
        return feuille;
    }

    private LigneHeureRequest buildRequest(LocalTime debut, LocalTime fin) {
        LigneHeureRequest req = new LigneHeureRequest();
        req.setDate(LocalDate.of(2026, 6, 15));
        req.setHeureDebut(debut);
        req.setHeureFin(fin);
        req.setAbsence(false);
        return req;
    }

    @Test
    @DisplayName("Ajout dépassant le VH → IllegalStateException avec message de dépassement")
    void depassement_VH_leve_exception() {
        // VH = 24h, déjà 22h déclarées, nouvelle séance = 4h → total 26h > 24h
        FeuilleHeure feuille = buildFeuille(24.0, 22.0);
        when(feuilleHeureRepository.findById(1L)).thenReturn(Optional.of(feuille));

        LigneHeureRequest req = buildRequest(
                LocalTime.of(8, 0), LocalTime.of(12, 0)); // 4h

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.ajouterLigne(1L, req));

        assertTrue(ex.getMessage().toLowerCase().contains("dépassement"),
                "Le message doit signaler un dépassement VH");
        verify(ligneHeureRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ajout dans le VH → séance enregistrée sans erreur")
    void dans_VH_ajout_accepte() {
        // VH = 24h, déjà 18h, nouvelle séance = 4h → total 22h < 24h
        FeuilleHeure feuille = buildFeuille(24.0, 18.0);
        when(feuilleHeureRepository.findById(2L)).thenReturn(Optional.of(feuille));
        when(ligneHeureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LigneHeureRequest req = buildRequest(
                LocalTime.of(8, 0), LocalTime.of(12, 0)); // 4h

        assertDoesNotThrow(() -> service.ajouterLigne(2L, req));
        verify(ligneHeureRepository).save(any());
    }

    @Test
    @DisplayName("Absence (REJETEE) ne consomme pas le VH — pas de dépassement signalé")
    void absence_ne_consomme_pas_VH() {
        // VH = 4h, déjà 4h, mais on ajoute une absence → pas de dépassement
        FeuilleHeure feuille = buildFeuille(4.0, 4.0);
        when(feuilleHeureRepository.findById(3L)).thenReturn(Optional.of(feuille));
        when(ligneHeureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LigneHeureRequest req = buildRequest(LocalTime.of(8, 0), LocalTime.of(12, 0));
        req.setAbsence(true);

        assertDoesNotThrow(() -> service.ajouterLigne(3L, req));
    }
}
