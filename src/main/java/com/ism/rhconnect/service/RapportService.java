package com.ism.rhconnect.service;

import com.ism.rhconnect.entity.Contrat;
import com.ism.rhconnect.entity.FeuilleHeure;
import com.ism.rhconnect.entity.Paiement;
import com.ism.rhconnect.repository.ContratRepository;
import com.ism.rhconnect.repository.FeuilleHeureRepository;
import com.ism.rhconnect.repository.PaiementRepository;
import com.itextpdf.io.font.PdfEncodings;
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
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RapportService {

    private static final String FONTS = "C:/Windows/Fonts/";
    private static final DeviceRgb BRUN  = new DeviceRgb(28, 8, 0);
    private static final DeviceRgb OR    = new DeviceRgb(237, 168, 50);
    private static final DeviceRgb GRIS  = new DeviceRgb(245, 245, 245);
    private static final float FN = 9f;

    private final ContratRepository contratRepository;
    private final FeuilleHeureRepository feuilleHeureRepository;
    private final PaiementRepository paiementRepository;

    // ── Rapport RP : résumé mensuel (PDF) ───────────────────────────────

    @Transactional(readOnly = true)
    public byte[] rapportRpMensuelPdf(String mois) throws Exception {
        List<FeuilleHeure> feuilles = feuilleHeureRepository.findAll().stream()
                .filter(f -> mois.equals(f.getPeriode()))
                .collect(Collectors.toList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfFont bold    = PdfFontFactory.createFont(FONTS + "timesbd.ttf", PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        PdfFont regular = PdfFontFactory.createFont(FONTS + "times.ttf",   PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(out));
             Document doc    = new Document(pdf, PageSize.A4.rotate())) {

            doc.setMargins(40, 40, 40, 40);

            // En-tête
            doc.add(new Paragraph("ISM Dakar — Rapport mensuel des vacations")
                    .setFont(bold).setFontSize(14).setFontColor(BRUN).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Période : " + mois)
                    .setFont(regular).setFontSize(FN).setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));
            SolidLine sep = new SolidLine(1.5f); sep.setColor(OR);
            doc.add(new LineSeparator(sep).setMarginBottom(12));

            // Tableau
            Table tbl = new Table(new float[]{2.5f, 2f, 1.5f, 1f, 1f, 1.2f, 1f, 1f})
                    .setWidth(UnitValue.createPercentValue(100));
            for (String h : new String[]{"Vacataire", "Module", "Classe", "Vol. Prévu (h)", "Heures val. (h)", "Écart (h)", "Taux FCFA/h", "Statut"}) {
                tbl.addHeaderCell(new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(8).setFontColor(OR))
                        .setBackgroundColor(BRUN).setBorder(new SolidBorder(OR, 0.5f))
                        .setPadding(4));
            }

            double totalPrevu = 0, totalRealise = 0;
            for (FeuilleHeure f : feuilles) {
                Contrat c = f.getContrat();
                double prevu    = c.getVolumeHorairePrevisionnel() != null ? c.getVolumeHorairePrevisionnel() : 0;
                double realise  = f.getTotalHeuresValidees() != null ? f.getTotalHeuresValidees() : 0;
                double ecart    = realise - prevu;
                totalPrevu   += prevu;
                totalRealise += realise;

                String nomVac = c.getVacataire().getUtilisateur().getPrenom() + " " + c.getVacataire().getUtilisateur().getNom();
                DeviceRgb bg = (tbl.getNumberOfRows() % 2 == 0) ? GRIS : null;

                tbl.addCell(cellule(nomVac, regular, bg));
                tbl.addCell(cellule(c.getModule(), regular, bg));
                tbl.addCell(cellule(c.getClasse(), regular, bg));
                tbl.addCell(cellule(String.format("%.1f", prevu), regular, bg));
                tbl.addCell(cellule(String.format("%.1f", realise), regular, bg));
                tbl.addCell(cellule(String.format("%+.1f", ecart), regular, ecart < 0 ? new DeviceRgb(220, 50, 50) : bg));
                tbl.addCell(cellule(c.getTauxHoraire() != null ? String.format("%.0f", c.getTauxHoraire()) : "—", regular, bg));
                tbl.addCell(cellule(f.getStatut().name(), regular, bg));
            }

            // Ligne totaux
            tbl.addCell(new com.itextpdf.layout.element.Cell(1, 3)
                    .add(new Paragraph("TOTAL").setFont(bold).setFontSize(FN).setTextAlignment(TextAlignment.RIGHT))
                    .setBackgroundColor(BRUN).setFontColor(OR).setBorder(new SolidBorder(OR, 0.5f)).setPadding(4));
            tbl.addCell(celluleBold(String.format("%.1f", totalPrevu), bold));
            tbl.addCell(celluleBold(String.format("%.1f", totalRealise), bold));
            tbl.addCell(celluleBold(String.format("%+.1f", totalRealise - totalPrevu), bold));
            tbl.addCell(new com.itextpdf.layout.element.Cell(1, 2)
                    .setBorder(new SolidBorder(OR, 0.5f)).setBackgroundColor(BRUN));

            doc.add(tbl);

            // Pied
            doc.add(new Paragraph("\nRapport généré le " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " — RHConnect ISM")
                    .setFont(regular).setFontSize(7).setFontColor(new DeviceRgb(150, 150, 150))
                    .setTextAlignment(TextAlignment.RIGHT).setMarginTop(10));
        }
        return out.toByteArray();
    }

    // ── Rapport Finance : paiements (PDF) ────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] rapportFinancePdf(String mois) throws Exception {
        List<Paiement> paiements = paiementRepository.findAll().stream()
                .filter(p -> mois.equals(p.getFeuilleHeure().getPeriode()))
                .collect(Collectors.toList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfFont bold    = PdfFontFactory.createFont(FONTS + "timesbd.ttf", PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        PdfFont regular = PdfFontFactory.createFont(FONTS + "times.ttf",   PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(out));
             Document doc    = new Document(pdf, PageSize.A4.rotate())) {

            doc.setMargins(40, 40, 40, 40);

            doc.add(new Paragraph("ISM Dakar — Rapport des paiements vacataires")
                    .setFont(bold).setFontSize(14).setFontColor(BRUN).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Période : " + mois)
                    .setFont(regular).setFontSize(FN).setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));
            SolidLine sep = new SolidLine(1.5f); sep.setColor(OR);
            doc.add(new LineSeparator(sep).setMarginBottom(12));

            Table tbl = new Table(new float[]{2.5f, 2f, 1.5f, 1f, 1f, 1.2f, 1f, 1f, 1f})
                    .setWidth(UnitValue.createPercentValue(100));
            for (String h : new String[]{"Vacataire", "Module", "Classe", "Heures", "Taux FCFA/h", "Brut FCFA", "Retenue 5%", "Net FCFA", "Statut"}) {
                tbl.addHeaderCell(new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(8).setFontColor(OR))
                        .setBackgroundColor(BRUN).setBorder(new SolidBorder(OR, 0.5f)).setPadding(4));
            }

            double totalBrut = 0, totalRetenue = 0, totalNet = 0;
            for (Paiement p : paiements) {
                FeuilleHeure f = p.getFeuilleHeure();
                Contrat c = f.getContrat();
                DeviceRgb bg = (tbl.getNumberOfRows() % 2 == 0) ? GRIS : null;
                String nom = c.getVacataire().getUtilisateur().getPrenom() + " " + c.getVacataire().getUtilisateur().getNom();
                totalBrut    += p.getMontantBrut();
                totalRetenue += p.getRetenueFiscale();
                totalNet     += p.getMontantNet();

                tbl.addCell(cellule(nom, regular, bg));
                tbl.addCell(cellule(c.getModule(), regular, bg));
                tbl.addCell(cellule(c.getClasse(), regular, bg));
                tbl.addCell(cellule(String.format("%.1f", p.getTotalHeures()), regular, bg));
                tbl.addCell(cellule(String.format("%.0f", p.getTauxHoraire()), regular, bg));
                tbl.addCell(cellule(String.format("%.0f", p.getMontantBrut()), regular, bg));
                tbl.addCell(cellule(String.format("%.0f", p.getRetenueFiscale()), regular, bg));
                tbl.addCell(cellule(String.format("%.0f", p.getMontantNet()), regular, bg));
                tbl.addCell(cellule(p.getStatut().name(), regular, bg));
            }

            tbl.addCell(new com.itextpdf.layout.element.Cell(1, 5)
                    .add(new Paragraph("TOTAL").setFont(bold).setFontSize(FN).setTextAlignment(TextAlignment.RIGHT))
                    .setBackgroundColor(BRUN).setFontColor(OR).setBorder(new SolidBorder(OR, 0.5f)).setPadding(4));
            tbl.addCell(celluleBold(String.format("%.0f", totalBrut), bold));
            tbl.addCell(celluleBold(String.format("%.0f", totalRetenue), bold));
            tbl.addCell(celluleBold(String.format("%.0f", totalNet), bold));
            tbl.addCell(new com.itextpdf.layout.element.Cell().setBorder(new SolidBorder(OR, 0.5f)).setBackgroundColor(BRUN));

            doc.add(tbl);

            doc.add(new Paragraph("\nRapport généré le " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " — RHConnect ISM")
                    .setFont(regular).setFontSize(7).setFontColor(new DeviceRgb(150, 150, 150))
                    .setTextAlignment(TextAlignment.RIGHT).setMarginTop(10));
        }
        return out.toByteArray();
    }

    // ── Rapport Finance : paiements (Excel) ──────────────────────────────

    @Transactional(readOnly = true)
    public byte[] rapportFinanceExcel(String mois) throws Exception {
        List<Paiement> paiements = paiementRepository.findAll().stream()
                .filter(p -> mois.equals(p.getFeuilleHeure().getPeriode()))
                .collect(Collectors.toList());

        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Paiements " + mois);

            // Styles
            CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font hFont = wb.createFont();
            hFont.setBold(true);
            hFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(hFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle totalStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font tFont = wb.createFont();
            tFont.setBold(true);
            totalStyle.setFont(tFont);

            // En-têtes
            Row header = sheet.createRow(0);
            String[] cols = {"Vacataire", "Module", "Classe", "Période", "Heures", "Taux FCFA/h", "Brut FCFA", "Retenue 5%", "Net FCFA", "Statut"};
            for (int i = 0; i < cols.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            double totalBrut = 0, totalRetenue = 0, totalNet = 0;
            int rowNum = 1;
            for (Paiement p : paiements) {
                FeuilleHeure f = p.getFeuilleHeure();
                Contrat c = f.getContrat();
                String nom = c.getVacataire().getUtilisateur().getPrenom() + " " + c.getVacataire().getUtilisateur().getNom();
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nom);
                row.createCell(1).setCellValue(c.getModule());
                row.createCell(2).setCellValue(c.getClasse());
                row.createCell(3).setCellValue(f.getPeriode());
                row.createCell(4).setCellValue(p.getTotalHeures());
                row.createCell(5).setCellValue(p.getTauxHoraire());
                row.createCell(6).setCellValue(p.getMontantBrut());
                row.createCell(7).setCellValue(p.getRetenueFiscale());
                row.createCell(8).setCellValue(p.getMontantNet());
                row.createCell(9).setCellValue(p.getStatut().name());
                totalBrut    += p.getMontantBrut();
                totalRetenue += p.getRetenueFiscale();
                totalNet     += p.getMontantNet();
            }

            // Ligne totaux
            Row total = sheet.createRow(rowNum);
            org.apache.poi.ss.usermodel.Cell labelCell = total.createCell(0);
            labelCell.setCellValue("TOTAL");
            labelCell.setCellStyle(totalStyle);
            org.apache.poi.ss.usermodel.Cell brutCell = total.createCell(6);
            brutCell.setCellValue(totalBrut);
            brutCell.setCellStyle(totalStyle);
            org.apache.poi.ss.usermodel.Cell retCell = total.createCell(7);
            retCell.setCellValue(totalRetenue);
            retCell.setCellStyle(totalStyle);
            org.apache.poi.ss.usermodel.Cell netCell = total.createCell(8);
            netCell.setCellValue(totalNet);
            netCell.setCellStyle(totalStyle);

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── Helpers PDF ───────────────────────────────────────────────────────

    private com.itextpdf.layout.element.Cell cellule(String texte, PdfFont font, DeviceRgb bg) {
        com.itextpdf.layout.element.Cell c = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(texte).setFont(font).setFontSize(FN))
                .setBorder(new SolidBorder(new DeviceRgb(220, 220, 220), 0.3f))
                .setPadding(3);
        if (bg != null) c.setBackgroundColor(bg);
        return c;
    }

    private com.itextpdf.layout.element.Cell celluleBold(String texte, PdfFont bold) {
        return new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(texte).setFont(bold).setFontSize(FN).setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(BRUN).setFontColor(OR)
                .setBorder(new SolidBorder(OR, 0.5f)).setPadding(4);
    }
}
