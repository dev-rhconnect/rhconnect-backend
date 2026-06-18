package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.FeuilleHeureRequest;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Détection de doublons de relevé — même contrat + même période")
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

    private Contrat contratActif;
    private Utilisateur attache;

    @BeforeEach
    void setup() {
        attache = Utilisateur.builder().id(1L).email("attache@ism.edu.sn").build();
        Utilisateur vacataireUser = Utilisateur.builder()
                .id(2L).prenom("Mansour").nom("Diallo").email("mansour@ism.edu.sn").build();
        Vacataire vacataire = Vacataire.builder().id(1L).utilisateur(vacataireUser).build();
        contratActif = Contrat.builder()
                .id(10L).statut(Contrat.StatutContrat.ACTIF)
                .module("Business English 1").classe("L3-GLRS")
                .vacataire(vacataire).build();

        // Simuler l'utilisateur connecté dans le SecurityContext
        var auth = new UsernamePasswordAuthenticationToken(
                attache.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(utilisateurRepository.findByEmail(attache.getEmail()))
                .thenReturn(Optional.of(attache));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Doublon détecté → IllegalStateException avec message explicite")
    void doublon_leve_exception() {
        when(contratRepository.findById(10L)).thenReturn(Optional.of(contratActif));
        when(feuilleHeureRepository.findByContratIdAndPeriode(10L, "2026-06"))
                .thenReturn(Optional.of(mock(FeuilleHeure.class)));

        FeuilleHeureRequest req = new FeuilleHeureRequest();
        req.setContratId(10L);
        req.setPeriode("2026-06");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.creerFeuille(req));

        assertTrue(ex.getMessage().contains("2026-06"),
                "Le message doit mentionner la période en doublon");
        verify(feuilleHeureRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pas de doublon → relevé créé avec statut EN_COURS")
    void sans_doublon_cree_releve() {
        when(contratRepository.findById(10L)).thenReturn(Optional.of(contratActif));
        when(feuilleHeureRepository.findByContratIdAndPeriode(10L, "2026-07"))
                .thenReturn(Optional.empty());
        when(feuilleHeureRepository.save(any())).thenAnswer(inv -> {
            FeuilleHeure f = inv.getArgument(0);
            return f;
        });

        FeuilleHeureRequest req = new FeuilleHeureRequest();
        req.setContratId(10L);
        req.setPeriode("2026-07");

        assertDoesNotThrow(() -> service.creerFeuille(req));
        verify(feuilleHeureRepository).save(any());
    }

    @Test
    @DisplayName("Contrat inactif → IllegalStateException")
    void contrat_inactif_bloque_creation() {
        Contrat inactif = Contrat.builder().id(11L)
                .statut(Contrat.StatutContrat.EXPIRE).build();
        when(contratRepository.findById(11L)).thenReturn(Optional.of(inactif));

        FeuilleHeureRequest req = new FeuilleHeureRequest();
        req.setContratId(11L);
        req.setPeriode("2026-06");

        assertThrows(IllegalStateException.class, () -> service.creerFeuille(req));
    }
}
