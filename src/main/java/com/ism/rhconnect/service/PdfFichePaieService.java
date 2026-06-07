package com.ism.rhconnect.service;

import com.ism.rhconnect.entity.Paiement;
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
public class PdfFichePaieService {

    private static final DeviceRgb BRUN       = new DeviceRgb(28, 8, 0);
    private static final DeviceRgb OR         = new DeviceRgb(200, 133, 0);
    private static final DeviceRgb GRIS_CLAIR  = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb GRIS_TEXTE  = new DeviceRgb(120, 120, 120);
    private static final DeviceRgb VERT        = new DeviceRgb(22, 163, 74);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] genererFichePaie(Paiement p) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(out));
             Document doc = new Document(pdf)) {

            Utilisateur u = p.getFeuilleHeure().getContrat().getVacataire().getUtilisateur();
            String module = p.getFeuilleHeure().getContrat().getModule();

            /* ── En-tête ── */
            doc.add(new Paragraph("RHCONNECT · INSTITUT SUPÉRIEUR DE MANAGEMENT")
                    .setFontColor(OR).setBold().setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("FICHE DE PAIE — VACATAIRE")
                    .setBold().setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(6));

            doc.add(new Paragraph("Période : " + p.getFeuilleHeure().getPeriode()
                    + "   ·   Module : " + module)
                    .setFontColor(GRIS_TEXTE).setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(16));

            /* ── Séparateur or ── */
            Table sep = new Table(1).setWidth(UnitValue.createPercentValue(100));
            sep.addCell(new Cell().setHeight(3).setBackgroundColor(OR).setBorder(Border.NO_BORDER));
            doc.add(sep);
            doc.add(new Paragraph(" ").setFontSize(4));

            /* ── Identité ── */
            doc.add(sectionTitre("IDENTIFICATION"));
            Table id = new Table(new float[]{1, 2}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(14);
            addLigne(id, "Employeur", "Institut Supérieur de Management (ISM) — Dakar");
            addLigne(id, "Vacataire", u.getPrenom() + " " + u.getNom());
            addLigne(id, "Email", u.getEmail());
            addLigne(id, "Module", module);
            addLigne(id, "Classe", p.getFeuilleHeure().getContrat().getClasse());
            doc.add(id);

            /* ── Détail de la rémunération ── */
            doc.add(sectionTitre("DÉTAIL DE LA RÉMUNÉRATION"));
            Table calcul = new Table(new float[]{2, 1, 1}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(14);

            // Header
            calcul.addHeaderCell(cell("Libellé", true, BRUN, true));
            calcul.addHeaderCell(cell("Quantité / Taux", true, BRUN, true));
            calcul.addHeaderCell(cell("Montant (FCFA)", true, BRUN, true));

            // Lignes
            calcul.addCell(cell("Heures effectuées et validées", false, null, false));
            calcul.addCell(cell(String.format("%.2f h × %.0f FCFA", p.getTotalHeures(), p.getTauxHoraire()), false, null, false));
            calcul.addCell(cell(String.format("%.0f", p.getMontantBrut()), false, null, false));

            calcul.addCell(cell("Retenue fiscale — CGI art. 200", false, null, false));
            calcul.addCell(cell("5 %", false, null, false));
            calcul.addCell(cell(String.format("− %.0f", p.getRetenueFiscale()), false, null, false));

            doc.add(calcul);

            /* ── Net à payer ── */
            Table net = new Table(new float[]{2, 1}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(20);
            net.addCell(new Cell().add(
                    new Paragraph("NET À PAYER").setBold().setFontSize(12).setFontColor(VERT))
                    .setBackgroundColor(new DeviceRgb(240, 253, 244)).setPadding(8).setBorder(Border.NO_BORDER));
            net.addCell(new Cell().add(
                    new Paragraph(String.format("%.0f FCFA", p.getMontantNet()))
                            .setBold().setFontSize(14).setFontColor(VERT)
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBackgroundColor(new DeviceRgb(240, 253, 244)).setPadding(8).setBorder(Border.NO_BORDER));
            doc.add(net);

            /* ── Mention légale ── */
            doc.add(new Paragraph(
                    "Rémunération calculée conformément au Code Général des Impôts du Sénégal (art. 200). " +
                    "La retenue fiscale de 5 % est reversée directement aux services fiscaux par l'employeur.")
                    .setFontColor(GRIS_TEXTE).setFontSize(8).setItalic().setMarginBottom(20));

            /* ── Pied de page ── */
            Table pied = new Table(1).setWidth(UnitValue.createPercentValue(100));
            pied.addCell(new Cell().setHeight(2).setBackgroundColor(GRIS_CLAIR).setBorder(Border.NO_BORDER));
            doc.add(pied);

            doc.add(new Paragraph("Fiche générée le " + LocalDate.now().format(FMT)
                    + " par RHConnect · Institut Supérieur de Management · Dakar, Sénégal")
                    .setFontColor(GRIS_TEXTE).setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(4));
        }

        return out.toByteArray();
    }

    private Paragraph sectionTitre(String titre) {
        return new Paragraph(titre)
                .setBold().setFontSize(10).setFontColor(BRUN)
                .setBackgroundColor(GRIS_CLAIR)
                .setPadding(5).setMarginBottom(4);
    }

    private void addLigne(Table t, String label, String val) {
        t.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(9)).setBackgroundColor(GRIS_CLAIR).setPadding(5));
        t.addCell(new Cell().add(new Paragraph(val).setFontSize(9)).setPadding(5));
    }

    private Cell cell(String text, boolean bold, DeviceRgb bgColor, boolean isHeader) {
        Paragraph p = new Paragraph(text).setFontSize(9);
        if (bold) p.setBold().setFontColor(DeviceRgb.WHITE);
        Cell c = new Cell().add(p).setPadding(6);
        if (bgColor != null) c.setBackgroundColor(bgColor);
        return c;
    }
}
