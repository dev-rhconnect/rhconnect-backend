package com.ism.rhconnect.service;

import com.ism.rhconnect.entity.Contrat;
import com.ism.rhconnect.entity.ContratModule;
import com.ism.rhconnect.entity.Utilisateur;
import com.ism.rhconnect.entity.Vacataire;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfContratService {

    private static final DeviceRgb BRUN_FONCE = new DeviceRgb(28,  8,   0);
    private static final DeviceRgb OR_ISM     = new DeviceRgb(200, 133, 0);
    private static final DeviceRgb GRIS_TEXTE = new DeviceRgb(110, 110, 110);
    private static final DeviceRgb GRIS_CLAIR = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb NOIR       = new DeviceRgb(0,   0,   0);

    private static final DateTimeFormatter FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final float FN  = 10f;
    private static final float FP  = 9f;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public byte[] genererContrat(Contrat c) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfFont regular = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        PdfFont italic  = PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC);

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(out));
             Document doc    = new Document(pdf, PageSize.A4)) {

            doc.setMargins(40, 50, 40, 50);

            Utilisateur u   = c.getVacataire().getUtilisateur();
            Vacataire   vac = c.getVacataire();
            String civilite    = "Monsieur";
            String nomPrenom   = u.getPrenom() + " " + u.getNom().toUpperCase();

            // ═══════════════════════════════════════
            //  PAGE 1 — PARTIES
            // ═══════════════════════════════════════
            entete(doc, bold, regular);
            separateur(doc);

            // Titre dans encadré
            String titre = c.isEstAvenant()
                    ? "AVENANT AU CONTRAT DE PRESTATIONS DE SERVICES"
                    : "CONTRAT DE PRESTATIONS DE SERVICES";
            doc.add(new Paragraph(titre)
                    .setFont(bold).setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorder(new SolidBorder(NOIR, 1f))
                    .setPaddingTop(6).setPaddingBottom(6)
                    .setMarginBottom(18));

            // Soussignés
            doc.add(new Paragraph("Entre les soussignés ").setFont(bold).setFontSize(FN)
                    .setUnderline().setMarginBottom(12));

            doc.add(new Paragraph()
                    .add(new Text("CAMPUS SAS").setFont(bold))
                    .add(new Text(", société de gestion de l'").setFont(regular))
                    .add(new Text("ISM").setFont(bold))
                    .add(new Text(" ayant son siège social au Point E, Rue 2, Dakar ; ci-après dénommée « Campus » représentée par Monsieur Abdou DIOUF, agissant en sa qualité de Directeur de CAMPUS SAS,").setFont(regular))
                    .setFontSize(FN).setMarginBottom(16));

            doc.add(new Paragraph("Ci-après dénommée « le bénéficiaire »")
                    .setFont(italic).setFontSize(FN)
                    .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(16));

            doc.add(new Paragraph("Et").setFont(bold).setFontSize(FN).setMarginBottom(12));

            // Identité vacataire
            doc.add(new Paragraph(civilite + " " + nomPrenom)
                    .setFont(bold).setFontSize(FN + 1).setMarginBottom(6));

            String dateNaiss = vac.getDateNaissance() != null
                    ? vac.getDateNaissance().format(FMT) : "—";
            String lieuNaiss = notEmpty(vac.getLieuNaissance(), "—");

            ligneIdentite(doc, bold, regular, "Date et lieu de naissance :", dateNaiss + " à " + lieuNaiss);
            ligneIdentite(doc, bold, regular, "Situation de famille :",
                    notEmpty(vac.getSituationMatrimoniale(), "—"));
            ligneIdentite(doc, bold, regular, "Lieu de résidence :",
                    notEmpty(vac.getAdresse(), "—"));
            ligneIdentite(doc, bold, regular, "Nationalité :",
                    notEmpty(vac.getNationalite(), "—"));
            ligneIdentite(doc, bold, regular, "N° NINEA :",
                    notEmpty(vac.getNinea(), "—"));

            doc.add(new Paragraph(" ").setFontSize(6));
            doc.add(new Paragraph("Ci-après dénommé « le prestataire »")
                    .setFont(italic).setFontSize(FN)
                    .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(10));

            piedDePage(doc, regular);

            // ═══════════════════════════════════════
            //  PAGE 2 — FICHE DE VACATIONS
            // ═══════════════════════════════════════
            doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            entete(doc, bold, regular);
            separateur(doc);

            doc.add(new Paragraph("FICHE DE VACATIONS")
                    .setFont(bold).setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(12));

            doc.add(new Paragraph(
                    "Il est confié au prestataire la mission d'enseignement dans le(s) module(s) suivants :")
                    .setFont(regular).setFontSize(FN).setMarginBottom(6));

            // Tableau modules
            Table tbl = new Table(new float[]{3f, 2.2f, 0.8f, 1.5f, 2.5f, 1.5f})
                    .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(12);

            for (String h : new String[]{"Modules", "Période", "VH", "Taux Horaires", "Classes", "Niveau"}) {
                tbl.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(FP)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBackgroundColor(GRIS_CLAIR)
                        .setBorder(new SolidBorder(NOIR, 0.5f))
                        .setPaddingTop(4).setPaddingBottom(4));
            }

            String periode = c.getDateDebut().format(FMT) + "\nAU\n" + c.getDateFin().format(FMT);
            String tauxStr = c.getTauxHoraire() != null
                    ? String.format(Locale.FRENCH, "%.0f", c.getTauxHoraire()) : "—";

            if (c.getModules() != null && !c.getModules().isEmpty()) {
                // Toutes les lignes partagent la même période et taux — fusionner si > 1 module
                int nbRows = c.getModules().size();
                boolean first = true;
                for (ContratModule cm : c.getModules()) {
                    String classes  = cm.getClasses() != null ? String.join("\n", cm.getClasses()) : "—";
                    String vh       = cm.getVolumeHorairePrevisionnel() != null
                            ? String.format("%.0f", cm.getVolumeHorairePrevisionnel()) + "H" : "—";
                    String niveauStr = cm.getNiveau() != null
                            ? niveauLabel(cm.getNiveau().name()) : "—";

                    tbl.addCell(cellFiche(cm.getNomModule(), regular, TextAlignment.LEFT));
                    if (first) {
                        tbl.addCell(new Cell(nbRows, 1)
                                .add(new Paragraph(periode).setFont(regular).setFontSize(FP)
                                        .setTextAlignment(TextAlignment.CENTER))
                                .setBorder(new SolidBorder(NOIR, 0.5f))
                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                .setPaddingTop(4).setPaddingBottom(4));
                    }
                    tbl.addCell(cellFiche(vh, regular, TextAlignment.CENTER));
                    if (first) {
                        tbl.addCell(new Cell(nbRows, 1)
                                .add(new Paragraph(tauxStr).setFont(regular).setFontSize(FP)
                                        .setTextAlignment(TextAlignment.CENTER))
                                .setBorder(new SolidBorder(NOIR, 0.5f))
                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                .setPaddingTop(4).setPaddingBottom(4));
                    }
                    tbl.addCell(cellFiche(classes, regular, TextAlignment.CENTER));
                    tbl.addCell(cellFiche(niveauStr, regular, TextAlignment.CENTER));
                    first = false;
                }
            } else {
                tbl.addCell(cellFiche(c.getModule() != null ? c.getModule() : "—", regular, TextAlignment.LEFT));
                tbl.addCell(cellFiche(periode, regular, TextAlignment.CENTER));
                tbl.addCell(cellFiche(c.getVolumeHorairePrevisionnel() != null
                        ? String.format("%.0f", c.getVolumeHorairePrevisionnel()) + "H" : "—", regular, TextAlignment.CENTER));
                tbl.addCell(cellFiche(tauxStr, regular, TextAlignment.CENTER));
                tbl.addCell(cellFiche("—", regular, TextAlignment.CENTER));
                tbl.addCell(cellFiche("—", regular, TextAlignment.CENTER));
            }
            doc.add(tbl);

            doc.add(new Paragraph()
                    .add(new Text("Ce(s) enseignement(s) se fera (feront) sous forme de cours magistral, " +
                                  "Travaux Dirigés (TD) et travaux Pratiques (TP) dans les programmes de ").setFont(regular))
                    .add(new Text("l'école d'ingénieurs – Digital Campus").setFont(bold))
                    .add(new Text(" du Groupe ISM.").setFont(regular))
                    .setFontSize(FN).setMarginBottom(8));

            doc.add(new Paragraph(
                    "Le prestataire atteste avoir pris connaissance de la nature de ces enseignements et " +
                    "déclare posséder les aptitudes professionnelles requises pour leur bonne exécution.")
                    .setFont(regular).setFontSize(FN).setMarginBottom(8));

            doc.add(new Paragraph(
                    "La présente fiche de vacations fait partie intégrante du contrat de prestation de " +
                    "services signés par les parties le prestataire et Campus SAS")
                    .setFont(regular).setFontSize(FN).setMarginBottom(20));

            // Signatures
            doc.add(new Paragraph("Fait à Dakar, le " + LocalDate.now().format(FMT))
                    .setFont(regular).setFontSize(FN)
                    .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(14));

            Table sig = new Table(new float[]{1f, 1f})
                    .setWidth(UnitValue.createPercentValue(100));

            Cell gauche = new Cell().setBorder(Border.NO_BORDER);
            gauche.add(new Paragraph("Le Prestataire").setFont(bold).setFontSize(FN).setUnderline());
            gauche.add(new Paragraph("Signature précédée de la mention\n\"Lu et approuvé\"")
                    .setFont(italic).setFontSize(8).setFontColor(GRIS_TEXTE));
            Image sigImg = chargerSignature(vac);
            if (sigImg != null) {
                sigImg.setMaxWidth(130).setMaxHeight(55).setMarginTop(6);
                gauche.add(sigImg);
            } else {
                gauche.add(new Paragraph("\n\n").setFontSize(FN));
            }
            gauche.add(new Paragraph("___________________________").setFont(regular).setFontSize(FN));

            Cell droite = new Cell().setBorder(Border.NO_BORDER);
            droite.add(new Paragraph("Pour Campus SAS ").setFont(bold).setFontSize(FN).setUnderline()
                    .setMarginBottom(6));
            droite.add(new Paragraph(" ").setFontSize(4));
            droite.add(new Paragraph("La Directrice de").setFont(regular).setFontSize(FN));
            droite.add(new Paragraph("L'école d'ingénieurs – Digital Campus").setFont(regular).setFontSize(FN)
                    .setUnderline().setMarginBottom(6));
            droite.add(new Paragraph(" ").setFontSize(4));
            droite.add(new Paragraph("Mame Diarra MBAYE").setFont(regular).setFontSize(FN));

            sig.addCell(gauche);
            sig.addCell(droite);
            doc.add(sig);

            piedDePage(doc, regular);
        }

        return out.toByteArray();
    }

    // ── En-tête ───────────────────────────────────────────────────────────

    private void entete(Document doc, PdfFont bold, PdfFont regular) throws Exception {
        Table h = new Table(new float[]{1.8f, 3f}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(2);

        Cell logoCell = new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        Image logo = chargerLogo();
        if (logo != null) {
            logo.setWidth(75).setHeight(75);
            logoCell.add(logo);
        } else {
            logoCell.setBackgroundColor(BRUN_FONCE)
                    .setBorder(new SolidBorder(OR_ISM, 3f))
                    .setPadding(8)
                    .add(new Paragraph("ISM").setFont(bold).setFontSize(22)
                            .setFontColor(new DeviceRgb(237, 168, 50)));
        }

        Cell infoCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        String[][] lignes = {
            {"RC : SN-DKR-2017-M-4479",   ""},
            {"NINEA : 001937054 2G2",      ""},
            {"Point E, Rue des Écrivains", ""},
            {"BP 5018, Dakar - Fann",      ""},
            {"Tél : +221 33 869 76 76",    ""},
            {"www.groupeism.sn",           "url"}
        };
        for (String[] l : lignes) {
            boolean url = l[1].equals("url");
            infoCell.add(new Paragraph(l[0])
                    .setFont(url ? bold : regular).setFontSize(8)
                    .setFontColor(url ? OR_ISM : GRIS_TEXTE).setMarginBottom(0));
        }

        h.addCell(logoCell);
        h.addCell(infoCell);
        doc.add(h);
    }

    private void separateur(Document doc) {
        SolidLine line = new SolidLine(1.5f);
        line.setColor(OR_ISM);
        doc.add(new LineSeparator(line).setMarginTop(4).setMarginBottom(14));
    }

    // ── Pied de page ──────────────────────────────────────────────────────

    private void piedDePage(Document doc, PdfFont regular) {
        doc.add(new Paragraph(" ").setFontSize(4));
        SolidLine line = new SolidLine(0.5f);
        line.setColor(OR_ISM);
        doc.add(new LineSeparator(line).setMarginBottom(4));
        doc.add(new Paragraph("Galileo  |  AUF  |  CAMES  |  AACSB  |  AMBA  |  EFMD")
                .setFont(regular).setFontSize(7).setFontColor(GRIS_TEXTE)
                .setTextAlignment(TextAlignment.CENTER));
    }

    // ── Chargement images ─────────────────────────────────────────────────

    private Image chargerLogo() {
        try {
            Path p = Paths.get(uploadDir, "ism-logo.png").toAbsolutePath();
            if (Files.exists(p)) return new Image(ImageDataFactory.create(p.toString()));
        } catch (Exception ignored) {}
        return null;
    }

    private Image chargerSignature(Vacataire vac) {
        try {
            if (vac.getCheminSignature() != null) {
                Path p = Paths.get(vac.getCheminSignature()).toAbsolutePath();
                if (Files.exists(p)) return new Image(ImageDataFactory.create(p.toString()));
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void ligneIdentite(Document doc, PdfFont bold, PdfFont regular,
                                String label, String valeur) {
        doc.add(new Paragraph()
                .add(new Text(label + "\t").setFont(regular))
                .add(new Text(valeur).setFont(bold))
                .setFontSize(FN).setMarginBottom(3));
    }

    private Cell cellFiche(String texte, PdfFont regular, TextAlignment align) {
        return new Cell()
                .add(new Paragraph(texte).setFont(regular).setFontSize(FP).setTextAlignment(align))
                .setBorder(new SolidBorder(NOIR, 0.5f))
                .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(4).setPaddingRight(4);
    }

    private String notEmpty(String val, String fallback) {
        return (val != null && !val.isBlank()) ? val : fallback;
    }

    private String niveauLabel(String niveau) {
        return switch (niveau) {
            case "L1"       -> "Licence 1";
            case "L2"       -> "Licence 2";
            case "L3"       -> "Licence 3";
            case "LICENCE"  -> "Licence";
            case "MASTER_1" -> "Master 1";
            case "MASTER_2" -> "Master 2";
            case "MASTER"   -> "Master";
            case "DUT"      -> "DUT";
            default         -> niveau;
        };
    }
}
