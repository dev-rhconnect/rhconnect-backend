package com.ism.rhconnect.service;

import com.ism.rhconnect.entity.*;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class PdfReleveService {


    // Couleurs
    private static final DeviceRgb BLEU       = new DeviceRgb(20,  50, 150);
    private static final DeviceRgb NOIR       = new DeviceRgb(0,   0,   0);
    private static final DeviceRgb GRIS_CLAIR = new DeviceRgb(240, 240, 240);
    private static final DeviceRgb GRIS_EN    = new DeviceRgb(220, 220, 220);
    private static final DeviceRgb VERT_VISA  = new DeviceRgb(22, 160, 70);

    // Bordure fine noire standard
    private static final SolidBorder BORDER  = new SolidBorder(NOIR, 0.5f);
    private static final SolidBorder BORDER1 = new SolidBorder(NOIR, 1f);

    @Value("${file.upload-dir}")
    private String uploadDir;

    public byte[] genererFicheDecompte(FeuilleHeure f) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont italic  = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        PdfFont boldIt  = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLDOBLIQUE);

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(out));
             Document doc    = new Document(pdf, PageSize.A4)) {

            doc.setMargins(28, 36, 28, 36);

            Contrat     c   = f.getContrat();
            Vacataire   vac = c.getVacataire();
            Utilisateur u   = vac.getUtilisateur();
            ContratModule cm = f.getContratModule();

            String nomVacataire    = u.getNom().toUpperCase();
            String prenomVacataire = capitalize(u.getPrenom());
            String telephone       = vac.getTelephone() != null ? vac.getTelephone() : "—";
            String specialite      = vac.getSpecialite() != null ? vac.getSpecialite() : (cm != null ? cm.getNomModule() : "—");
            String nomModule       = cm != null ? cm.getNomModule() : (c.getModule() != null ? c.getModule() : "—");

            List<String> classes = cm != null && cm.getClasses() != null && !cm.getClasses().isEmpty()
                    ? cm.getClasses()
                    : (c.getClasse() != null ? List.of(c.getClasse()) : List.of());
            String classesBenef = String.join(", ", classes);

            // Année académique et mois depuis la période "2026-05"
            String[] parts = f.getPeriode().split("-");
            int annee = Integer.parseInt(parts[0]);
            int mois  = Integer.parseInt(parts[1]);
            String anneeAcad = mois >= 9
                    ? (annee + " / " + (annee + 1))
                    : ((annee - 1) + " / " + annee);
            String moisStr = new java.util.Date(annee - 1900, mois - 1, 1)
                    .toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    .getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH)
                    .toUpperCase();

            // Cadre d'intervention (heuristique)
            boolean isDigitalCampus = (specialite.toUpperCase().contains("DIGITAL")
                    || classesBenef.toUpperCase().contains("DC")
                    || classesBenef.toUpperCase().contains("DIGITAL"));
            String cadreEcole   = isDigitalCampus ? "ÉCOLE D'INGÉNIEURS  □" : "ÉCOLE D'INGÉNIEURS  ☑";
            String cadreDigital = isDigitalCampus ? "DIGITAL CAMPUS  ☑"    : "DIGITAL CAMPUS  □";

            // Lignes de séances
            List<LigneHeure> lignes = f.getLignes() != null ? f.getLignes() : List.of();
            int totalSeances  = lignes.size();
            double totalHeures = lignes.stream().mapToDouble(l -> l.getDuree() != null ? l.getDuree() : 0).sum();

            // Taux et montant
            Double taux = c.getTauxHoraire();
            String tauxStr    = taux != null ? String.format("%,.0f F", taux) : "—";
            String montantStr = taux != null ? String.format("%,.0f FCFA", totalHeures * taux) : "—";

            // Visa de l'assistant
            String nomAttache = f.getAttache().getPrenom() + " " + f.getAttache().getNom();
            String dateVisa   = f.getDateSoumission() != null
                    ? f.getDateSoumission().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    : LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            // ═══════════════════════════════════════════════════════
            //  SECTION 1 — BANDEAU TITRE
            // ═══════════════════════════════════════════════════════
            Table titre = new Table(new float[]{60, 280, 180}).setWidth(UnitValue.createPercentValue(100));
            titre.setMarginBottom(0);

            // Cellule ISM
            titre.addCell(cell(2, 1, TextAlignment.CENTER, bold, 13f, NOIR)
                    .add(new Paragraph("ISM").setFont(bold).setFontSize(13).setFontColor(NOIR)
                            .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0))
                    .setBorder(BORDER1).setVerticalAlignment(VerticalAlignment.MIDDLE));

            // Cellule titre
            titre.addCell(new Cell().setBorder(BORDER1)
                    .add(new Paragraph("FICHE DE DÉCOMPTE").setFont(bold).setFontSize(15).setFontColor(NOIR)
                            .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0))
                    .add(new Paragraph("HORAIRE").setFont(bold).setFontSize(15).setFontColor(NOIR)
                            .setTextAlignment(TextAlignment.CENTER).setMarginTop(0))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPadding(6));

            // Cellule référence
            Table refTable = new Table(new float[]{90, 90}).setWidth(UnitValue.createPercentValue(100));
            refTable.addCell(cellNoBorder(bold, 8f, NOIR, TextAlignment.LEFT).add(para("Réf : E22. Anim", regular, 8f, NOIR)));
            refTable.addCell(cellNoBorder(bold, 8f, NOIR, TextAlignment.LEFT).add(para("Motif : Modification", regular, 8f, NOIR)));
            refTable.addCell(cellNoBorder(bold, 8f, NOIR, TextAlignment.LEFT).add(para("Version N° : 02", regular, 8f, NOIR)));
            refTable.addCell(cellNoBorder(bold, 8f, NOIR, TextAlignment.LEFT).add(para("Date : 03/02/17", regular, 8f, NOIR)));
            refTable.addCell(cellNoBorder(bold, 8f, NOIR, TextAlignment.LEFT).add(para("Page : 1/1", regular, 8f, NOIR)));
            refTable.addCell(cellNoBorder(bold, 8f, NOIR, TextAlignment.LEFT).add(para("", regular, 8f, NOIR)));

            titre.addCell(new Cell().setBorder(BORDER1).add(refTable)
                    .setPaddingLeft(4).setPaddingTop(4));

            doc.add(titre);

            // ═══════════════════════════════════════════════════════
            //  SECTION 2 — INFORMATIONS PERSONNELLES
            // ═══════════════════════════════════════════════════════
            Table info = new Table(new float[]{260, 260}).setWidth(UnitValue.createPercentValue(100));
            info.setMarginTop(0).setMarginBottom(0);

            // Nom / Prénoms
            info.addCell(infoCell(bold, italic, "Nom : ", nomVacataire, 10f));
            info.addCell(infoCell(bold, italic, "Prénoms : ", prenomVacataire, 10f));

            // Statut / Téléphone
            info.addCell(infoCell(bold, italic, "Statut : Externe ☑    ", "", 10f));
            info.addCell(infoCell(bold, italic, "Téléphone : ", telephone, 10f));

            // Cadre d'Intervention
            Cell cellCadreLabel = new Cell(1, 1).setBorder(BORDER)
                    .setPadding(6).setVerticalAlignment(VerticalAlignment.MIDDLE);
            cellCadreLabel.add(new Paragraph("Cadre\nd'Intervention")
                    .setFont(bold).setFontSize(9f).setFontColor(NOIR)
                    .setTextAlignment(TextAlignment.CENTER));

            Cell cellCadreVal = new Cell(1, 1).setBorder(BORDER).setPadding(6)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            cellCadreVal.add(new Paragraph()
                    .add(new Text(cadreEcole + "     ").setFont(bold).setFontSize(9f).setFontColor(NOIR))
                    .add(new Text(cadreDigital).setFont(bold).setFontSize(9f).setFontColor(NOIR))
                    .setTextAlignment(TextAlignment.CENTER));
            info.addCell(cellCadreLabel);
            info.addCell(cellCadreVal);

            // Classe(s) / Spécialisation
            info.addCell(infoCell(bold, italic, "Classe(s) bénéficiaire(s) : ", classesBenef, 10f));
            info.addCell(infoCell(bold, italic, "Spécialisation : ", specialite, 10f));

            // Année Académique / Module(s)
            info.addCell(infoCell(bold, italic, "Année Académique : ", anneeAcad, 10f));
            Cell cellModule = new Cell().setBorder(BORDER).setPadding(6);
            cellModule.add(new Paragraph()
                    .add(new Text("Module(s)\nenseigné(s) : ").setFont(bold).setFontSize(9f).setFontColor(NOIR))
                    .add(new Text(nomModule).setFont(italic).setFontSize(10f).setFontColor(BLEU)));
            info.addCell(cellModule);

            // Mois
            info.addCell(infoCell(bold, italic, "Mois : ", moisStr, 10f));
            info.addCell(new Cell().setBorder(BORDER).setPadding(6)
                    .add(new Paragraph(" ").setFontSize(10f)));

            doc.add(info);

            // ═══════════════════════════════════════════════════════
            //  SECTION 3 — TABLEAU DES SÉANCES
            // ═══════════════════════════════════════════════════════
            // Colonnes : Date | Début h | Début mn | Fin h | Fin mn | Durée | Signature
            Table seancesTable = new Table(new float[]{70, 28, 28, 28, 28, 50, 100})
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(0);

            // Ligne 1 d'en-tête — Date (rowspan 2) + Début (colspan 2) + Fin (colspan 2) + Durée (rowspan 2) + Signature (rowspan 2)
            seancesTable.addHeaderCell(enteteCellRowspan("Date", bold, 2));
            seancesTable.addHeaderCell(enteteCell("Début", bold, 2));
            seancesTable.addHeaderCell(enteteCell("Fin", bold, 2));
            seancesTable.addHeaderCell(enteteCellRowspan("Durée", bold, 2));
            seancesTable.addHeaderCell(new Cell(2, 1).setBorder(BORDER).setBackgroundColor(GRIS_EN)
                    .add(new Paragraph("Signature de\nl'intervenant")
                            .setFont(bold).setFontSize(8f).setFontColor(NOIR)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(4));

            // Ligne 2 d'en-tête — h mn h mn
            for (int i = 0; i < 4; i++) {
                seancesTable.addHeaderCell(new Cell().setBorder(BORDER).setBackgroundColor(GRIS_EN)
                        .add(new Paragraph(i % 2 == 0 ? "h" : "mn")
                                .setFont(bold).setFontSize(8f).setFontColor(NOIR)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setPadding(3));
            }

            // Lignes de données
            String nomSignature = "M. " + u.getNom();
            int LIGNES_MIN = 14;
            DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern("dd-MM-yy");

            for (LigneHeure l : lignes) {
                seancesTable.addCell(dataCell(l.getDate().format(fmtDate), italic, BLEU));
                seancesTable.addCell(dataCell(String.format("%02d", l.getHeureDebut().getHour()), italic, BLEU));
                seancesTable.addCell(dataCell(String.format("%02d", l.getHeureDebut().getMinute()), italic, BLEU));
                seancesTable.addCell(dataCell(String.format("%02d", l.getHeureFin().getHour()), italic, BLEU));
                seancesTable.addCell(dataCell(String.format("%02d", l.getHeureFin().getMinute()), italic, BLEU));
                seancesTable.addCell(dataCell(formatDuree(l.getDuree()), italic, BLEU));
                seancesTable.addCell(dataCell(nomSignature, italic, BLEU));
            }

            // Lignes vides pour remplir
            int nbVides = Math.max(0, LIGNES_MIN - lignes.size());
            for (int i = 0; i < nbVides; i++) {
                for (int col = 0; col < 7; col++) {
                    seancesTable.addCell(new Cell().setBorder(BORDER).setHeight(16f)
                            .add(new Paragraph(" ").setFontSize(9f)));
                }
            }

            doc.add(seancesTable);

            // ═══════════════════════════════════════════════════════
            //  SECTION 4 — TOTAUX
            // ═══════════════════════════════════════════════════════
            Table totaux = new Table(new float[]{260, 260}).setWidth(UnitValue.createPercentValue(100));

            totaux.addCell(infoCell(bold, italic, "Durée Total : ", formatDureeH(totalHeures), 10f));
            totaux.addCell(infoCell(bold, italic, "Taux horaire brut : ", tauxStr, 10f));

            Cell cellMontant = new Cell(1, 2).setBorder(BORDER).setPadding(6);
            cellMontant.add(new Paragraph()
                    .add(new Text("Montant brut : ").setFont(bold).setFontSize(10f).setFontColor(NOIR))
                    .add(new Text(montantStr).setFont(italic).setFontSize(11f).setFontColor(BLEU)));
            totaux.addCell(cellMontant);

            doc.add(totaux);

            // ═══════════════════════════════════════════════════════
            //  SECTION 5 — VISAS
            // ═══════════════════════════════════════════════════════
            Table visas = new Table(new float[]{175, 175, 170}).setWidth(UnitValue.createPercentValue(100));

            // Visa Assistant de Prog.
            Cell cellVisa1 = new Cell().setBorder(BORDER).setPadding(8);
            boolean estSoumis = f.getStatut() != FeuilleHeure.Statut.EN_COURS;
            cellVisa1.add(new Paragraph("Date " + dateVisa + " / " + annee)
                    .setFont(regular).setFontSize(8f).setFontColor(NOIR).setMarginBottom(2));
            cellVisa1.add(new Paragraph("Visa de l'Assistant de Prog.")
                    .setFont(bold).setFontSize(9f).setFontColor(NOIR).setMarginBottom(6));
            if (estSoumis) {
                cellVisa1.add(new Paragraph(nomAttache)
                        .setFont(boldIt).setFontSize(12f).setFontColor(BLEU).setMarginBottom(2));
                cellVisa1.add(new Paragraph("✓ Visé")
                        .setFont(bold).setFontSize(8f).setFontColor(VERT_VISA));
            } else {
                cellVisa1.add(new Paragraph("En attente")
                        .setFont(italic).setFontSize(9f).setFontColor(new DeviceRgb(150, 150, 150)));
            }

            // Visa Coordonnateur
            Cell cellVisa2 = new Cell().setBorder(BORDER).setPadding(8);
            cellVisa2.add(new Paragraph("Date ....../ ......... / " + annee)
                    .setFont(regular).setFontSize(8f).setFontColor(NOIR).setMarginBottom(2));
            cellVisa2.add(new Paragraph("Visa du Coordonnateur")
                    .setFont(bold).setFontSize(9f).setFontColor(NOIR).setMarginBottom(6));
            cellVisa2.add(new Paragraph(
                    f.getStatut() == FeuilleHeure.Statut.VALIDE_RP
                    || f.getStatut() == FeuilleHeure.Statut.SOUMIS_FINANCE
                    || f.getStatut() == FeuilleHeure.Statut.VALIDE ? "Validé" : "En attente")
                    .setFont(italic).setFontSize(9f)
                    .setFontColor(f.getStatut() == FeuilleHeure.Statut.VALIDE_RP
                            || f.getStatut() == FeuilleHeure.Statut.SOUMIS_FINANCE
                            || f.getStatut() == FeuilleHeure.Statut.VALIDE
                            ? VERT_VISA : new DeviceRgb(150, 150, 150)));

            // Visa Directeur
            Cell cellVisa3 = new Cell().setBorder(BORDER).setPadding(8);
            cellVisa3.add(new Paragraph("Date ....../ ......... / " + annee)
                    .setFont(regular).setFontSize(8f).setFontColor(NOIR).setMarginBottom(2));
            cellVisa3.add(new Paragraph("Visa du Directeur")
                    .setFont(bold).setFontSize(9f).setFontColor(NOIR).setMarginBottom(6));
            cellVisa3.add(new Paragraph(
                    f.getStatut() == FeuilleHeure.Statut.VALIDE ? "Validé" : "En attente")
                    .setFont(italic).setFontSize(9f)
                    .setFontColor(f.getStatut() == FeuilleHeure.Statut.VALIDE
                            ? VERT_VISA : new DeviceRgb(150, 150, 150)));

            visas.addCell(cellVisa1);
            visas.addCell(cellVisa2);
            visas.addCell(cellVisa3);
            doc.add(visas);
        }

        return out.toByteArray();
    }

    /* ── Helpers ── */


    private Cell cell(int rowspan, int colspan, TextAlignment align, PdfFont font, float size, DeviceRgb color) {
        return new Cell(rowspan, colspan).setBorder(BORDER)
                .setPadding(4).setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell cellNoBorder(PdfFont font, float size, DeviceRgb color, TextAlignment align) {
        return new Cell().setBorder(Border.NO_BORDER).setPadding(1);
    }

    private Paragraph para(String text, PdfFont font, float size, DeviceRgb color) {
        return new Paragraph(text).setFont(font).setFontSize(size).setFontColor(color);
    }

    /** Cellule label:valeur avec italique bleu pour la valeur. */
    private Cell infoCell(PdfFont bold, PdfFont italic, String label, String value, float size) {
        return new Cell().setBorder(BORDER).setPadding(6)
                .add(new Paragraph()
                        .add(new Text(label).setFont(bold).setFontSize(size - 1).setFontColor(NOIR))
                        .add(new Text(value).setFont(italic).setFontSize(size).setFontColor(BLEU)));
    }

    /** En-tête de colonne centré avec fond gris, rowspan 1. */
    private Cell enteteCell(String text, PdfFont bold, int colspan) {
        return new Cell(1, colspan).setBorder(BORDER).setBackgroundColor(GRIS_EN)
                .add(new Paragraph(text).setFont(bold).setFontSize(8.5f).setFontColor(NOIR)
                        .setTextAlignment(TextAlignment.CENTER))
                .setPadding(4).setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    /** En-tête de colonne centré avec fond gris, rowspan 2. */
    private Cell enteteCellRowspan(String text, PdfFont bold, int rowspan) {
        return new Cell(rowspan, 1).setBorder(BORDER).setBackgroundColor(GRIS_EN)
                .add(new Paragraph(text).setFont(bold).setFontSize(8.5f).setFontColor(NOIR)
                        .setTextAlignment(TextAlignment.CENTER))
                .setPadding(4).setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    /** Cellule de données centrée en italique bleu. */
    private Cell dataCell(String text, PdfFont italic, DeviceRgb color) {
        return new Cell().setBorder(BORDER).setPadding(3)
                .add(new Paragraph(text).setFont(italic).setFontSize(9.5f).setFontColor(color)
                        .setTextAlignment(TextAlignment.CENTER))
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    /** Formate une durée en "03H" ou "02H30" */
    private String formatDuree(double duree) {
        int h = (int) duree;
        int mn = (int) Math.round((duree - h) * 60);
        return mn > 0 ? String.format("%02dH%02d", h, mn) : String.format("%02dH", h);
    }

    /** Formate une durée totale en "22 h" */
    private String formatDureeH(double duree) {
        int h = (int) duree;
        int mn = (int) Math.round((duree - h) * 60);
        return mn > 0 ? h + " h " + mn + " mn" : h + " h";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
