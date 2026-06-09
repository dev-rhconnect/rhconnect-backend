package com.ism.rhconnect.service;

import com.ism.rhconnect.entity.Contrat;
import com.ism.rhconnect.entity.Utilisateur;
import com.ism.rhconnect.entity.Vacataire;
import com.itextpdf.io.font.PdfEncodings;
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

@Service
public class PdfContratService {

    private static final DeviceRgb BRUN_FONCE = new DeviceRgb(28,  8,   0);
    private static final DeviceRgb OR_ISM      = new DeviceRgb(237, 168, 50);
    private static final DeviceRgb OR_SOMBRE   = new DeviceRgb(200, 133, 0);
    private static final DeviceRgb GRIS_CLAIR  = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb GRIS_TEXTE  = new DeviceRgb(110, 110, 110);

    private static final String FONTS = "C:/Windows/Fonts/";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final float FN = 10f;
    private static final float FP = 8.5f;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public byte[] genererContrat(Contrat c) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfFont regular = PdfFontFactory.createFont(FONTS + "times.ttf",   PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        PdfFont bold    = PdfFontFactory.createFont(FONTS + "timesbd.ttf", PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        PdfFont italic  = PdfFontFactory.createFont(FONTS + "timesi.ttf",  PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(out));
             Document doc    = new Document(pdf, PageSize.A4)) {

            doc.setMargins(40, 50, 40, 50);

            Utilisateur u     = c.getVacataire().getUtilisateur();
            Vacataire   vac   = c.getVacataire();
            String nomComplet = "M. " + u.getPrenom().toUpperCase() + " " + u.getNom().toUpperCase();

            // ═══════════════════════════════════════
            //  PAGE 1 — CONTRAT
            // ═══════════════════════════════════════
            entete(doc, bold, regular, italic);
            separateur(doc);

            doc.add(new Paragraph("IL A ÉTÉ CONVENU CE QUI SUIT :")
                    .setFont(bold).setFontSize(FN).setMarginBottom(14));

            // Art. 1
            doc.add(titreBold("Article 1er : Objet et durée du contrat", bold));
            doc.add(new Paragraph()
                    .add(new Text(nomComplet).setFont(bold))
                    .add(new Text(" loue ses services en qualité d'enseignant, à ISM Dakar.").setFont(bold))
                    .setFontSize(FN).setMarginBottom(6));
            doc.add(para(
                "Les prestations qui lui seront en général demandées figurent à l'annexe du présent contrat, " +
                "intitulée \"FICHE DE VACATIONS\" étant entendu que cette annexe fait partie intégrante du présent contrat.",
                regular));
            doc.add(new Paragraph()
                    .add(new Text("Le présent contrat entrera en vigueur à compter du ").setFont(regular))
                    .add(new Text(c.getDateDebut().format(FMT)).setFont(bold))
                    .add(new Text(", et prend fin dans les trois semaines qui suivent la remise des copies et " +
                                  "notes d'évaluation et/ou d'examen par le vacataire.").setFont(regular))
                    .setFontSize(FN).setMarginBottom(6));
            doc.add(new Paragraph("Il ne sera renouvelé que par un écrit signé des deux parties.")
                    .setFont(bold).setFontSize(FN).setMarginBottom(12));

            // Art. 2
            doc.add(titreBold("Article 2. Obligations du prestataire", bold));
            doc.add(para("Le prestataire consent également à intervenir aussi bien en présentiel qu'en ligne.", regular));
            doc.add(para("Il s'engage donc à se conformer aux règles internes et aux conditions générales de travail de l'ISM Dakar", regular));
            doc.add(para("Il s'engage de ce fait à respecter :", regular));
            doc.add(bullet("Son planning de cours tel que décliné dans l'école de référence", regular));
            doc.add(bullet("Le délai des remises des évaluations et notes", regular));
            doc.add(new Paragraph(
                    "Le prestataire s'engage donc de façon générale à accomplir au mieux sa mission dans la loyauté " +
                    "et dans le respect du présent contrat et des recommandations qui lui seront faites par le bénéficiaire.")
                    .setFont(regular).setFontSize(FN).setMarginTop(4).setMarginBottom(12));

            // Art. 3
            doc.add(titreBold("Article 3 : Obligation du bénéficiaire", bold));
            doc.add(para(
                "Le bénéficiaire devra, pendant l'exécution du présent contrat, fournir au prestataire de service, " +
                "les moyens matériels et l'aide raisonnablement demandés par le prestataire pour lui permettre " +
                "d'exécuter convenablement ses prestations.",
                regular));

            // Art. 4
            doc.add(titreBold("Article 4 : Honoraires", bold));
            doc.add(new Paragraph()
                    .add(new Text("Les honoraires du prestataire sont forfaitairement fixés, pour l'ensemble des " +
                                  "prestations requises, à la somme de ").setFont(regular))
                    .add(new Text(String.format("%.0f FCFA/Heure", c.getTauxHoraire())).setFont(bold))
                    .add(new Text(", payable à la fin de chaque mois.").setFont(regular))
                    .setFontSize(FN).setMarginBottom(6));
            doc.add(para(
                "Le règlement sera effectué par virement bancaire contre présentation de l'original " +
                "de la facture, adressée au bénéficiaire.",
                regular));

            // Art. 5
            doc.add(titreBold("Article 5 : Assurance et charges fiscales", bold));
            doc.add(para(nomComplet +
                " déclare être un prestataire indépendant puisqu'étant enseignant dans d'autres " +
                "établissements d'enseignement privés et/ou publics et, de ce fait, il ne peut invoquer " +
                "ou se prévaloir des droits et obligations reconnus à un travailleur au sens de l'article 2 " +
                "du Code du Travail ;",
                regular));
            doc.add(para("Il s'engage donc à supporter toutes les charges fiscales découlant de cette qualité de prestataire indépendant ;", regular));
            doc.add(para(
                "Toutefois, conformément à l'article 200 du Code Général des Impôts, une retenue de 5% " +
                "du montant des honoraires sera effectuée et reversée à l'Administration Fiscale.",
                regular));
            doc.add(para(
                "Le Bénéficiaire s'engage en outre à souscrire les assurances nécessaires pour couvrir " +
                "les risques encourus du fait de l'accomplissement de sa mission et en sera seul responsable.",
                regular));

            // Art. 6
            doc.add(titreBold("Article 6 : Secret professionnel et confidentialité", bold));
            doc.add(para(
                "Le prestataire est tenu au secret professionnel. Il ne peut divulguer les informations " +
                "auxquelles il accédera dans le cadre des services fournis au bénéficiaire.",
                regular));
            doc.add(para(
                "Tous les documents utiles à l'exécution de la présente convention revêtent un caractère " +
                "confidentiel et ne peuvent être divulgués à des tiers, même après la résiliation du contrat, " +
                "sans l'autorisation préalable et écrite du bénéficiaire.",
                regular));
            doc.add(para(
                "Chaque partie pourra également résilier unilatéralement le présent contrat par l'envoi " +
                "d'une lettre recommandée avec avis de réception, en respectant un préavis de 3 mois.",
                regular));

            // Art. 7
            doc.add(titreBold("Article 7 : Clause de propriété", bold));
            doc.add(para(
                "Tous les rapports, études et autres documents issus de travaux commandités par l'ISM et " +
                "produits par le prestataire dans le cadre de l'exécution de sa mission restent la propriété " +
                "exclusive du bénéficiaire, à qui ils devront être impérativement remis en cas de rupture du " +
                "présent contrat pour quelque cause que ce soit.",
                regular));

            // Art. 8
            doc.add(titreBold("Article 8 : Résiliation", bold));
            doc.add(para("À l'arrivée du terme, le contrat prendra fin automatiquement sans formalité aucune.", regular));
            doc.add(para(
                "Tout manquement du prestataire à une de ses obligations, notamment celles visées à l'article 2 " +
                "ci-dessus et à l'annexe ci-jointe, entraînera la résiliation de plein droit du présent contrat.",
                regular));

            // Art. 9
            doc.add(titreBold("Article 9 : Différend", bold));
            doc.add(para(
                "Les parties feront de leur mieux pour régler à l'amiable les différends qui pourraient " +
                "surgir lors de l'exécution du présent contrat.",
                regular));
            doc.add(para(
                "Faute de règlement amiable, le Tribunal de Grande Instance Hors Classe de Dakar sera compétent.",
                regular));

            doc.add(new Paragraph(" ").setFontSize(5));
            signatures(doc, bold, regular, italic, nomComplet, vac);

            // ═══════════════════════════════════════
            //  PAGE 2 — FICHE DE VACATIONS
            // ═══════════════════════════════════════
            doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            entete(doc, bold, regular, italic);
            separateur(doc);

            doc.add(new Paragraph("FICHE DE VACATIONS")
                    .setFont(bold).setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(12));

            doc.add(para("Il est confié au prestataire la mission d'enseignement dans le(s) module(s) suivants :", regular));

            // Tableau
            Table tbl = new Table(new float[]{3f, 2.2f, 0.8f, 1.5f, 2.5f, 1.5f})
                    .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(12);
            for (String h : new String[]{"Modules", "Période", "VH", "Taux Horaires", "Classes", "Niveau"}) {
                tbl.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(FP)
                                .setFontColor(OR_ISM).setTextAlignment(TextAlignment.CENTER))
                        .setBackgroundColor(BRUN_FONCE)
                        .setBorder(new SolidBorder(OR_ISM, 0.5f))
                        .setPaddingTop(5).setPaddingBottom(5));
            }
            tbl.addCell(celleFiche(c.getModule(), regular));
            tbl.addCell(celleFiche(c.getDateDebut().format(FMT) + " au\n" + c.getDateFin().format(FMT), regular));
            tbl.addCell(celleFiche(c.getVolumeHorairePrevisionnel() != null
                    ? String.format("%.0f", c.getVolumeHorairePrevisionnel()) : "-", regular));
            tbl.addCell(celleFiche(c.getTauxHoraire() != null
                    ? String.format("%.0f", c.getTauxHoraire()) : "-", regular));
            tbl.addCell(celleFiche(c.getClasse(), regular));
            tbl.addCell(celleFiche("-", regular));
            doc.add(tbl);

            doc.add(new Paragraph()
                    .add(new Text("Ce(s) enseignement(s) se fera (feront) sous forme de cours magistral, " +
                                  "Travaux Dirigés (TD) et travaux Pratiques (TP) dans les programmes de ").setFont(regular))
                    .add(new Text("l'ISM Dakar").setFont(bold))
                    .add(new Text(".").setFont(regular))
                    .setFontSize(FN).setMarginBottom(8));
            doc.add(para(
                "Le prestataire atteste avoir pris connaissance de la nature de ces enseignements et déclare " +
                "posséder les aptitudes professionnelles requises pour leur bonne exécution.",
                regular));
            doc.add(para(
                "La présente fiche de vacations fait partie intégrante du contrat de prestation de services " +
                "signé par les parties le prestataire et l'ISM Dakar",
                regular));

            doc.add(new Paragraph(" ").setFontSize(5));
            signatures(doc, bold, regular, italic, nomComplet, vac);
            piedDePage(doc, regular);
        }

        return out.toByteArray();
    }

    // ── En-tête ───────────────────────────────────────────────────────────

    private void entete(Document doc, PdfFont bold, PdfFont regular, PdfFont italic) throws Exception {
        Table h = new Table(new float[]{1.8f, 3f}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(2);

        Cell logoCell = new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        Image logo = chargerLogo();
        if (logo != null) {
            logo.setWidth(70).setHeight(70);
            logoCell.add(logo);
        } else {
            logoCell.setBackgroundColor(BRUN_FONCE)
                    .setBorder(new SolidBorder(OR_ISM, 3f))
                    .setPadding(8)
                    .add(new Paragraph("ISM").setFont(bold).setFontSize(22).setFontColor(OR_ISM))
                    .add(new Paragraph("Institut Supérieur de Management")
                            .setFont(italic).setFontSize(7).setFontColor(OR_SOMBRE));
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
                    .setFontColor(url ? OR_SOMBRE : GRIS_TEXTE).setMarginBottom(0));
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

    // ── Signatures ────────────────────────────────────────────────────────

    private void signatures(Document doc, PdfFont bold, PdfFont regular, PdfFont italic,
                             String nomComplet, Vacataire vac) throws Exception {
        doc.add(new Paragraph("Fait à Dakar, le " + LocalDate.now().format(FMT))
                .setFont(regular).setFontSize(FN)
                .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(14));

        Table sig = new Table(new float[]{1f, 1f}).setWidth(UnitValue.createPercentValue(100));

        Cell gauche = new Cell().setBorder(Border.NO_BORDER);
        gauche.add(new Paragraph("LE PRESTATAIRE").setFont(bold).setFontSize(FN).setUnderline());
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
        gauche.add(new Paragraph(nomComplet).setFont(regular).setFontSize(8).setFontColor(GRIS_TEXTE));

        Cell droite = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        droite.add(new Paragraph("Pour ISM Dakar").setFont(bold).setFontSize(FN).setUnderline());
        droite.add(new Paragraph("\nLe Directeur Général").setFont(regular).setFontSize(FN));
        droite.add(new Paragraph("Institut Supérieur de Management\n\n")
                .setFont(italic).setFontSize(8).setFontColor(GRIS_TEXTE));
        droite.add(new Paragraph("___________________________").setFont(regular).setFontSize(FN));

        sig.addCell(gauche);
        sig.addCell(droite);
        doc.add(sig);
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

    private Paragraph titreBold(String texte, PdfFont bold) {
        return new Paragraph(texte).setFont(bold).setFontSize(FN).setMarginBottom(4);
    }

    private Paragraph para(String texte, PdfFont regular) {
        return new Paragraph(texte).setFont(regular).setFontSize(FN).setMarginBottom(6);
    }

    private Paragraph bullet(String texte, PdfFont regular) {
        return new Paragraph("•  " + texte)
                .setFont(regular).setFontSize(FN).setMarginLeft(20).setMarginBottom(2);
    }

    private Cell celleFiche(String texte, PdfFont regular) {
        return new Cell()
                .add(new Paragraph(texte).setFont(regular).setFontSize(FP)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(new SolidBorder(GRIS_CLAIR, 0.5f))
                .setPaddingTop(5).setPaddingBottom(5);
    }
}
