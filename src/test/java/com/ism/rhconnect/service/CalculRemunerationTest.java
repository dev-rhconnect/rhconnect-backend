package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.PaiementRequest;
import com.ism.rhconnect.entity.*;
import com.ism.rhconnect.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Moteur de calcul de rémunération — CGI art. 200 (retenue 5%)")
class CalculRemunerationTest {

    @InjectMocks  PaieService service;
    @Mock PaiementRepository     paiementRepository;
    @Mock FeuilleHeureRepository feuilleHeureRepository;
    @Mock PdfFichePaieService    pdfFichePaieService;
    @Mock EmailService           emailService;
    @Mock NotificationService    notificationService;
    @Mock UtilisateurRepository  utilisateurRepository;
    @Captor ArgumentCaptor<Paiement> paiementCaptor;

    // ── helpers ────────────────────────────────────────────────────────────────

    private FeuilleHeure feuilleValide(double heures) {
        Utilisateur u  = Utilisateur.builder().id(1L).prenom("Test").nom("User").email("t@t.com").build();
        Vacataire    v  = Vacataire.builder().id(1L).utilisateur(u).build();
        Contrat      c  = Contrat.builder().id(1L).vacataire(v).module("M").classe("C").build();
        FeuilleHeure f  = mock(FeuilleHeure.class);
        when(f.getStatut()).thenReturn(FeuilleHeure.Statut.VALIDE);
        when(f.getTotalHeuresValidees()).thenReturn(heures);
        when(f.getContrat()).thenReturn(c);
        when(f.getPeriode()).thenReturn("2026-06");
        return f;
    }

    private PaiementRequest request(Long feuilleId, double taux) {
        PaiementRequest r = new PaiementRequest();
        r.setFeuilleHeureId(feuilleId);
        r.setTauxHoraire(taux);
        return r;
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Taux Licence (10 000 FCFA/h) × 24 h → brut 240 000 | retenue 12 000 | net 228 000")
    void licence_10000_24h() {
        FeuilleHeure feuille = feuilleValide(24.0);
        when(feuilleHeureRepository.findById(1L)).thenReturn(Optional.of(feuille));
        when(paiementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.calculer(request(1L, 10_000.0));

        verify(paiementRepository).save(paiementCaptor.capture());
        Paiement p = paiementCaptor.getValue();
        assertEquals(240_000.0, p.getMontantBrut(),    0.01, "Montant brut incorrect");
        assertEquals( 12_000.0, p.getRetenueFiscale(), 0.01, "Retenue CGI incorrecte");
        assertEquals(228_000.0, p.getMontantNet(),     0.01, "Net à payer incorrect");
    }

    @Test
    @DisplayName("Taux Master (15 000 FCFA/h) × 20 h → brut 300 000 | retenue 15 000 | net 285 000")
    void master_15000_20h() {
        FeuilleHeure feuille = feuilleValide(20.0);
        when(feuilleHeureRepository.findById(2L)).thenReturn(Optional.of(feuille));
        when(paiementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.calculer(request(2L, 15_000.0));

        verify(paiementRepository).save(paiementCaptor.capture());
        Paiement p = paiementCaptor.getValue();
        assertEquals(300_000.0, p.getMontantBrut(),    0.01);
        assertEquals( 15_000.0, p.getRetenueFiscale(), 0.01);
        assertEquals(285_000.0, p.getMontantNet(),     0.01);
    }

    @Test
    @DisplayName("Taux Docteur (25 000 FCFA/h) × 16 h → brut 400 000 | retenue 20 000 | net 380 000")
    void docteur_25000_16h() {
        FeuilleHeure feuille = feuilleValide(16.0);
        when(feuilleHeureRepository.findById(3L)).thenReturn(Optional.of(feuille));
        when(paiementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.calculer(request(3L, 25_000.0));

        verify(paiementRepository).save(paiementCaptor.capture());
        Paiement p = paiementCaptor.getValue();
        assertEquals(400_000.0, p.getMontantBrut(),    0.01);
        assertEquals( 20_000.0, p.getRetenueFiscale(), 0.01);
        assertEquals(380_000.0, p.getMontantNet(),     0.01);
    }

    @Test
    @DisplayName("Relevé non validé → IllegalStateException")
    void releve_non_valide_leve_exception() {
        FeuilleHeure feuille = mock(FeuilleHeure.class);
        when(feuille.getStatut()).thenReturn(FeuilleHeure.Statut.SOUMIS);
        when(feuilleHeureRepository.findById(99L)).thenReturn(Optional.of(feuille));

        assertThrows(IllegalStateException.class, () -> service.calculer(request(99L, 10_000.0)));
        verify(paiementRepository, never()).save(any());
    }
}
