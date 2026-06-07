package com.ism.rhconnect.service;

import com.ism.rhconnect.entity.Contrat;
import com.ism.rhconnect.entity.Utilisateur;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PdfContratService {

    private static final DeviceRgb BRUN      = new DeviceRgb(28, 8, 0);
    private static final DeviceRgb OR        = new DeviceRgb(200, 133, 0);
    private static final DeviceRgb GRIS_CLAIR = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb GRIS_TEXTE = new DeviceRgb(120, 120, 120);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] genererContrat(Contrat c) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(out));
             Document doc = new Document(pdf)) {

            Utilisateur u = c.getVacataire().getUtilisateur();

            /* ── En-tête ── */
            doc.add(new Paragraph("RHCONNECT · INSTITUT SUPÉRIEUR DE MANAGEMENT")
                    .setFontColor(OR).setBold().setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("CONTRAT DE VACATION")
                    .setBold().setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(8));

            doc.add(new Paragraph("Référence : RHC-VAC-" + String.format("%05d", c.getId()))
                    .setFontColor(GRIS_TEXTE).setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            /* ── Séparateur ── */
            Table sep = new Table(1).setWidth(UnitValue.createPercentValue(100));
            sep.addCell(new Cell().setHeight(3).setBackgroundColor(OR).setBorder(Border.NO_BORDER));
            doc.add(sep);
            doc.add(new Paragraph(" ").setFontSize(4));

            /* ── Parties ── */
            doc.add(section("PARTIES AU CONTRAT"));

            Table parties = new Table(new float[]{1, 2}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(12);
            addLigne(parties, "Employeur", "Institut Supérieur de Management (ISM) — Dakar, Sénégal");
            addLigne(parties, "Vacataire", u.getPrenom() + " " + u.getNom());
            addLigne(parties, "Email", u.getEmail());
            doc.add(parties);

            /* ── Objet du contrat ── */
            doc.add(section("OBJET DU CONTRAT"));

            Table objet = new Table(new float[]{1, 2}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(12);
            addLigne(objet, "Module", c.getModule());
            addLigne(objet, "Classe", c.getClasse());
            addLigne(objet, "Volume horaire", c.getVolumeHorairePrevisionnel() + " heures");
            addLigne(objet, "Taux horaire", String.format("%.0f FCFA / heure", c.getTauxHoraire()));
            addLigne(objet, "Rémunération brute prévisionnelle",
                    String.format("%.0f FCFA", c.getVolumeHorairePrevisionnel() * c.getTauxHoraire()));
            addLigne(objet, "Date de début", c.getDateDebut().format(FMT));
            addLigne(objet, "Date de fin", c.getDateFin().format(FMT));
            addLigne(objet, "Type", c.isEstAvenant() ? "Avenant au contrat n° RHC-VAC-"
                    + String.format("%05d", c.getContratParent() != null ? c.getContratParent().getId() : 0) : "Contrat initial");
            doc.add(objet);

            /* ── Conditions générales ── */
            doc.add(section("CONDITIONS GÉNÉRALES"));

            String[] articles = {
                "Art. 1 — Objet : Le vacataire s'engage à dispenser les enseignements du module et de la classe mentionnés " +
                        "ci-dessus, selon le volume horaire convenu, conformément au calendrier académique de l'ISM.",
                "Art. 2 — Rémunération : La rémunération est calculée sur la base des heures effectivement dispensées et " +
                        "validées par le Responsable de Programme, au taux horaire indiqué.",
                "Art. 3 — Retenue fiscale : Une retenue à la source de 5 % sera appliquée sur la rémunération brute, " +
                        "conformément à l'article 200 du Code Général des Impôts du Sénégal.",
                "Art. 4 — Relevé mensuel : Le vacataire s'engage à soumettre son relevé mensuel des heures effectuées " +
                        "avant le 5 de chaque mois suivant, via la plateforme RHConnect.",
                "Art. 5 — Confidentialité : Le vacataire s'engage à respecter la confidentialité des informations " +
                        "académiques et administratives auxquelles il aura accès.",
                "Art. 6 — Résiliation : Le présent contrat peut être résilié par l'une ou l'autre des parties avec un " +
                        "préavis de 15 jours, sauf cas de force majeure."
            };

            for (String art : articles) {
                doc.add(new Paragraph(art).setFontSize(9).setMarginBottom(4));
            }

            /* ── Signatures ── */
            doc.add(new Paragraph(" "));
            doc.add(section("SIGNATURES"));

            Table sig = new Table(new float[]{1, 1}).setWidth(UnitValue.createPercentValue(100)).setMarginTop(8);

            Cell employeur = new Cell().setBorder(Border.NO_BORDER).add(
                    new Paragraph("Pour ISM Dakar\nLe Responsable de Programme\n\n\n\n___________________________")
                            .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            Cell vacataire = new Cell().setBorder(Border.NO_BORDER).add(
                    new Paragraph("Le Vacataire\n" + u.getPrenom() + " " + u.getNom() + "\n\n\n\n___________________________")
                            .setFontSize(10).setTextAlignment(TextAlignment.CENTER));

            sig.addCell(employeur);
            sig.addCell(vacataire);
            doc.add(sig);

            /* ── Pied de page ── */
            doc.add(new Paragraph(" "));
            Table pied = new Table(1).setWidth(UnitValue.createPercentValue(100));
            pied.addCell(new Cell().setHeight(2).setBackgroundColor(GRIS_CLAIR).setBorder(Border.NO_BORDER));
            doc.add(pied);

            doc.add(new Paragraph("Document généré automatiquement par RHConnect · ISM Dakar · "
                    + LocalDate.now().format(FMT))
                    .setFontColor(GRIS_TEXTE).setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(4));
        }

        return out.toByteArray();
    }

    private Paragraph section(String titre) {
        return new Paragraph(titre)
                .setBold().setFontSize(10).setFontColor(BRUN)
                .setBackgroundColor(GRIS_CLAIR)
                .setPadding(5).setMarginBottom(6);
    }

    private void addLigne(Table table, String label, String valeur) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setBold().setFontSize(9))
                .setBackgroundColor(GRIS_CLAIR).setPadding(5));
        table.addCell(new Cell()
                .add(new Paragraph(valeur).setFontSize(9))
                .setPadding(5));
    }
}
