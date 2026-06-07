package com.ism.rhconnect.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String expediteur;

    @Async
    public void envoyerContrat(String destinataire, String nomVacataire,
                               String module, byte[] pdfBytes) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(expediteur, "RHConnect ISM");
            helper.setTo(destinataire);
            helper.setSubject("Votre contrat de vacation — " + module);
            helper.setText(htmlContrat(nomVacataire, module), true);
            helper.addAttachment(
                    "Contrat_" + module.replaceAll("\\s+", "_") + ".pdf",
                    new ByteArrayResource(pdfBytes),
                    "application/pdf");
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email contrat : " + e.getMessage(), e);
        }
    }

    @Async
    public void envoyerFichePaie(String destinataire, String nomVacataire,
                                  String periode, byte[] pdfBytes) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(expediteur, "RHConnect ISM");
            helper.setTo(destinataire);
            helper.setSubject("Votre fiche de paie — " + periode);
            helper.setText(htmlFichePaie(nomVacataire, periode), true);
            helper.addAttachment(
                    "FichePaie_" + periode.replaceAll("\\s+", "_") + ".pdf",
                    new ByteArrayResource(pdfBytes),
                    "application/pdf");
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email fiche de paie : " + e.getMessage(), e);
        }
    }

    @Async
    public void envoyerNotification(String destinataire, String sujet, String corps) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(expediteur, "RHConnect ISM");
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(htmlNotification(corps), true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi notification : " + e.getMessage(), e);
        }
    }

    /* ── Templates HTML ── */

    private String htmlContrat(String nom, String module) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;">
              <div style="background:#1C0800;padding:24px;border-radius:8px 8px 0 0;">
                <h1 style="color:#C88500;margin:0;font-size:22px;">RH Connect · ISM Dakar</h1>
                <p style="color:rgba(255,255,255,0.5);margin:4px 0 0;font-size:13px;">Portail vacataires</p>
              </div>
              <div style="background:#fafafa;padding:32px;border-radius:0 0 8px 8px;border:1px solid #eee;">
                <p style="margin:0 0 16px;">Bonjour <strong>%s</strong>,</p>
                <p>Veuillez trouver ci-joint votre <strong>contrat de vacation</strong> pour le module :</p>
                <div style="background:#FEF5EC;border-left:4px solid #C88500;padding:12px 16px;margin:16px 0;border-radius:0 6px 6px 0;">
                  <strong style="color:#1C0800;">%s</strong>
                </div>
                <p>Merci de le lire attentivement. Pour toute question, contactez votre responsable de programme.</p>
                <hr style="border:none;border-top:1px solid #eee;margin:24px 0;" />
                <p style="font-size:12px;color:#aaa;margin:0;">Institut Supérieur de Management · Dakar, Sénégal<br>Ce message est généré automatiquement par RHConnect.</p>
              </div>
            </div>""".formatted(nom, module);
    }

    private String htmlFichePaie(String nom, String periode) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;">
              <div style="background:#1C0800;padding:24px;border-radius:8px 8px 0 0;">
                <h1 style="color:#C88500;margin:0;font-size:22px;">RH Connect · ISM Dakar</h1>
              </div>
              <div style="background:#fafafa;padding:32px;border-radius:0 0 8px 8px;border:1px solid #eee;">
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Votre <strong>fiche de paie</strong> pour la période <strong>%s</strong> est disponible en pièce jointe.</p>
                <hr style="border:none;border-top:1px solid #eee;margin:24px 0;" />
                <p style="font-size:12px;color:#aaa;margin:0;">Institut Supérieur de Management · Dakar, Sénégal</p>
              </div>
            </div>""".formatted(nom, periode);
    }

    private String htmlNotification(String corps) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;">
              <div style="background:#1C0800;padding:24px;border-radius:8px 8px 0 0;">
                <h1 style="color:#C88500;margin:0;font-size:22px;">RH Connect · ISM Dakar</h1>
              </div>
              <div style="background:#fafafa;padding:32px;border-radius:0 0 8px 8px;border:1px solid #eee;">
                <p>%s</p>
                <hr style="border:none;border-top:1px solid #eee;margin:24px 0;" />
                <p style="font-size:12px;color:#aaa;margin:0;">Institut Supérieur de Management · Dakar, Sénégal</p>
              </div>
            </div>""".formatted(corps);
    }
}
