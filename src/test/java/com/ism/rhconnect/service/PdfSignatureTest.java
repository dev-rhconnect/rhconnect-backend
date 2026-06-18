package com.ism.rhconnect.service;

import com.ism.rhconnect.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Injection de signature dans le contrat PDF — iText 7")
class PdfSignatureTest {

    @TempDir
    Path tempDir;

    private PdfContratService pdfContratService;

    @BeforeEach
    void setup() {
        pdfContratService = new PdfContratService();
        ReflectionTestUtils.setField(pdfContratService, "uploadDir", tempDir.toString());
    }

    private Contrat buildContrat(String cheminSignature) throws Exception {
        Utilisateur u = Utilisateur.builder()
                .id(1L).prenom("Mansour").nom("Diallo")
                .email("mansour.diallo@ism.edu.sn").build();
        Vacataire v = Vacataire.builder()
                .id(1L).utilisateur(u)
                .cheminSignature(cheminSignature)
                .build();
        return Contrat.builder()
                .id(1L)
                .vacataire(v)
                .module("Business English 1")
                .classe("L3-GLRS")
                .tauxHoraire(10_000.0)
                .volumeHorairePrevisionnel(24.0)
                .dateDebut(LocalDate.of(2026, 1, 1))
                .dateFin(LocalDate.of(2026, 6, 30))
                .statut(Contrat.StatutContrat.ACTIF)
                .build();
    }

    @Test
    @DisplayName("Contrat avec signature présente → PDF généré non vide")
    void contrat_avec_signature_genere_pdf() throws Exception {
        // Créer un fichier image de signature factice dans le répertoire temporaire
        Path signatureFile = tempDir.resolve("signature_1.png");
        // PNG 1×1 pixel transparent minimal (valide pour iText)
        byte[] pngMinimal = new byte[]{
            (byte)0x89,0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A,
            0x00,0x00,0x00,0x0D,0x49,0x48,0x44,0x52,
            0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x01,
            0x08,0x06,0x00,0x00,0x00,0x1F,0x15,(byte)0xC4,
            (byte)0x89,0x00,0x00,0x00,0x0A,0x49,0x44,0x41,
            0x54,0x78,(byte)0x9C,0x62,0x00,0x01,0x00,0x00,
            0x05,0x00,0x01,0x0D,0x0A,0x2D,(byte)0xB4,0x00,
            0x00,0x00,0x00,0x49,0x45,0x4E,0x44,(byte)0xAE,
            0x42,0x60,(byte)0x82
        };
        Files.write(signatureFile, pngMinimal);

        Contrat contrat = buildContrat(signatureFile.toString());
        byte[] pdf = pdfContratService.genererContrat(contrat);

        assertNotNull(pdf, "Le PDF ne doit pas être null");
        assertTrue(pdf.length > 0, "Le PDF ne doit pas être vide");
        // Un PDF commence toujours par %PDF
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    @Test
    @DisplayName("Contrat sans signature → PDF généré (signature absente mais contrat valide)")
    void contrat_sans_signature_genere_pdf() throws Exception {
        Contrat contrat = buildContrat(null);
        byte[] pdf = pdfContratService.genererContrat(contrat);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
