package com.ism.rhconnect.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String expediteur;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /* ═══════════════════════════════════════════════════════
       ENVOIS
    ═══════════════════════════════════════════════════════ */

    @Async
    public void envoyerBienvenue(String destinataire, String prenom, String nom,
                                  String role, String mdpTemporaire) {
        envoyer(destinataire,
                "Bienvenue sur RHConnect — vos identifiants de connexion",
                htmlBienvenue(prenom, nom, role, destinataire, mdpTemporaire));
    }

    @Async
    public void envoyerResetMotDePasse(String destinataire, String prenom, String nom,
                                        String mdpTemporaire) {
        envoyer(destinataire,
                "RHConnect — Réinitialisation de votre mot de passe",
                htmlResetMdp(prenom, nom, destinataire, mdpTemporaire));
    }

    @Async
    public void envoyerContrat(String destinataire, String nomVacataire,
                                String module, byte[] pdfBytes) {
        envoyerAvecPj(destinataire,
                "Votre contrat de vacation — " + module,
                htmlContrat(nomVacataire, module),
                "Contrat_" + module.replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".pdf",
                pdfBytes);
    }

    @Async
    public void envoyerFichePaie(String destinataire, String nomVacataire,
                                  String periode, byte[] pdfBytes) {
        envoyerAvecPj(destinataire,
                "Votre bulletin de paie — " + periode,
                htmlFichePaie(nomVacataire, periode),
                "Bulletin_" + periode.replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".pdf",
                pdfBytes);
    }

    @Async
    public void envoyerReleveSoumis(String destinataire, String prenom,
                                     String module, String periode) {
        envoyer(destinataire,
                "Confirmation — Relevé d'heures soumis (" + periode + ")",
                htmlReleveSoumis(prenom, module, periode));
    }

    @Async
    public void envoyerReleveValide(String destinataire, String prenom,
                                     String module, String periode, String totalHeures) {
        envoyer(destinataire,
                "Relevé d'heures validé — " + module + " (" + periode + ")",
                htmlReleveValide(prenom, module, periode, totalHeures));
    }

    @Async
    public void envoyerReleveRejete(String destinataire, String prenom,
                                     String module, String periode, String motif) {
        envoyer(destinataire,
                "Action requise — Relevé d'heures retourné (" + periode + ")",
                htmlReleveRejete(prenom, module, periode, motif));
    }

    /* ═══════════════════════════════════════════════════════
       MÉTHODES INTERNES D'ENVOI
    ═══════════════════════════════════════════════════════ */

    private void envoyer(String destinataire, String sujet, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, false, "UTF-8");
            h.setFrom(expediteur, "RHConnect · ISM Dakar");
            h.setTo(destinataire);
            h.setSubject(sujet);
            h.setText(html, true);
            mailSender.send(msg);
            log.info("Email envoyé à {} — {}", destinataire, sujet);
        } catch (Exception e) {
            log.error("Échec envoi e-mail à {} [{}] : {}", destinataire, sujet, e.getMessage());
        }
    }

    private void envoyerAvecPj(String destinataire, String sujet, String html,
                                 String nomFichier, byte[] bytes) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
            h.setFrom(expediteur, "RHConnect · ISM Dakar");
            h.setTo(destinataire);
            h.setSubject(sujet);
            h.setText(html, true);
            h.addAttachment(nomFichier, new ByteArrayResource(bytes), "application/pdf");
            mailSender.send(msg);
            log.info("Email avec PJ envoyé à {} — {}", destinataire, sujet);
        } catch (Exception e) {
            log.error("Échec envoi e-mail avec PJ à {} [{}] : {}", destinataire, sujet, e.getMessage());
        }
    }

    /* ═══════════════════════════════════════════════════════
       TEMPLATES HTML
    ═══════════════════════════════════════════════════════ */

    private String htmlBienvenue(String prenom, String nom, String role,
                                  String email, String mdp) {
        String roleLabel = switch (role) {
            case "RESPONSABLE_PROGRAMME"  -> "Responsable de Programme";
            case "RELAIS_FINANCE"         -> "Relais Finance";
            case "ATTACHE_ENSEIGNEMENT"   -> "Attaché d'Enseignement";
            case "VACATAIRE"              -> "Vacataire";
            default                       -> role;
        };
        return wrap("""
            <p style="margin:0 0 8px;font-size:15px;color:#444;">Madame, Monsieur <strong style="color:#1C0800;">%s %s</strong>,</p>
            <p style="color:#555;line-height:1.7;">
              Votre compte a été créé sur la plateforme <strong>RHConnect</strong>, le portail de gestion
              des ressources humaines de l'<strong>Institut Supérieur de Management</strong>.
            </p>
            <p style="color:#555;line-height:1.7;">Vous êtes enregistré(e) avec le profil suivant :</p>

            %s

            <p style="color:#555;line-height:1.7;margin-top:24px;">
              Voici vos identifiants de première connexion :
            </p>

            <table style="width:100%%;border-collapse:collapse;margin:8px 0 24px;">
              <tr>
                <td style="padding:10px 14px;background:#f5f5f5;border:1px solid #e0e0e0;font-size:13px;color:#777;width:140px;">Identifiant</td>
                <td style="padding:10px 14px;background:#fff;border:1px solid #e0e0e0;font-size:14px;color:#1C0800;font-weight:600;">%s</td>
              </tr>
              <tr>
                <td style="padding:10px 14px;background:#f5f5f5;border:1px solid #e0e0e0;font-size:13px;color:#777;">Mot de passe</td>
                <td style="padding:10px 14px;background:#fff;border:1px solid #e0e0e0;font-size:14px;color:#1C0800;font-weight:600;letter-spacing:1px;">%s</td>
              </tr>
            </table>

            <div style="background:#FFFBF0;border:1px solid #EDA832;border-radius:6px;padding:14px 18px;margin-bottom:24px;">
              <p style="margin:0;font-size:13px;color:#7A5100;">
                <strong>Important :</strong> Vous serez invité(e) à choisir un nouveau mot de passe personnel
                dès votre première connexion. Conservez ces identifiants confidentiels.
              </p>
            </div>

            <div style="text-align:center;margin:28px 0;">
              <a href="%s/login" style="display:inline-block;background:#1C0800;color:#EDA832;text-decoration:none;padding:13px 32px;border-radius:6px;font-size:14px;font-weight:600;letter-spacing:0.5px;">
                Accéder à la plateforme
              </a>
            </div>

            <p style="color:#555;line-height:1.7;font-size:13px;">
              Pour toute difficulté de connexion, veuillez contacter l'Administration IT de l'ISM.
            </p>
            """.formatted(
                prenom, nom,
                badgeProfil(roleLabel, email),
                email, mdp,
                frontendUrl
            ));
    }

    private String htmlResetMdp(String prenom, String nom, String email, String mdp) {
        return wrap("""
            <p style="margin:0 0 8px;font-size:15px;color:#444;">Madame, Monsieur <strong style="color:#1C0800;">%s %s</strong>,</p>
            <p style="color:#555;line-height:1.7;">
              Suite à une demande de réinitialisation, votre mot de passe sur la plateforme
              <strong>RHConnect</strong> a été réinitialisé par l'Administration IT.
            </p>
            <p style="color:#555;line-height:1.7;">Voici vos nouveaux identifiants temporaires :</p>

            <table style="width:100%%;border-collapse:collapse;margin:8px 0 24px;">
              <tr>
                <td style="padding:10px 14px;background:#f5f5f5;border:1px solid #e0e0e0;font-size:13px;color:#777;width:140px;">Identifiant</td>
                <td style="padding:10px 14px;background:#fff;border:1px solid #e0e0e0;font-size:14px;color:#1C0800;font-weight:600;">%s</td>
              </tr>
              <tr>
                <td style="padding:10px 14px;background:#f5f5f5;border:1px solid #e0e0e0;font-size:13px;color:#777;">Nouveau mot de passe</td>
                <td style="padding:10px 14px;background:#fff;border:1px solid #e0e0e0;font-size:14px;color:#1C0800;font-weight:600;letter-spacing:1px;">%s</td>
              </tr>
            </table>

            <div style="background:#FFFBF0;border:1px solid #EDA832;border-radius:6px;padding:14px 18px;margin-bottom:24px;">
              <p style="margin:0;font-size:13px;color:#7A5100;">
                <strong>Important :</strong> Vous serez invité(e) à définir un mot de passe personnel
                dès votre prochaine connexion. Si vous n'êtes pas à l'origine de cette demande,
                contactez immédiatement l'Administration IT.
              </p>
            </div>

            <div style="text-align:center;margin:28px 0;">
              <a href="%s/login" style="display:inline-block;background:#1C0800;color:#EDA832;text-decoration:none;padding:13px 32px;border-radius:6px;font-size:14px;font-weight:600;letter-spacing:0.5px;">
                Se connecter
              </a>
            </div>
            """.formatted(prenom, nom, email, mdp, frontendUrl));
    }

    private String htmlContrat(String nom, String module) {
        return wrap("""
            <p style="margin:0 0 8px;font-size:15px;color:#444;">Madame, Monsieur <strong style="color:#1C0800;">%s</strong>,</p>
            <p style="color:#555;line-height:1.7;">
              Nous avons le plaisir de vous adresser ci-joint votre <strong>contrat de vacation</strong>
              pour le module ci-dessous, dans le cadre de vos interventions au sein de l'Institut Supérieur de Management.
            </p>

            <div style="background:#FEF5EC;border-left:4px solid #EDA832;padding:14px 20px;margin:20px 0;border-radius:0 6px 6px 0;">
              <p style="margin:0;font-size:11px;text-transform:uppercase;letter-spacing:1px;color:#C88500;font-weight:600;">Module</p>
              <p style="margin:6px 0 0;font-size:16px;font-weight:700;color:#1C0800;">%s</p>
            </div>

            <p style="color:#555;line-height:1.7;">
              Nous vous invitons à lire attentivement ce document et à le retourner signé
              à votre Responsable de Programme dans les meilleurs délais.
            </p>
            <p style="color:#555;line-height:1.7;">
              Pour toute question relative à ce contrat, veuillez vous rapprocher du service RH
              ou de votre responsable de programme.
            </p>
            """.formatted(nom, module));
    }

    private String htmlFichePaie(String nom, String periode) {
        return wrap("""
            <p style="margin:0 0 8px;font-size:15px;color:#444;">Madame, Monsieur <strong style="color:#1C0800;">%s</strong>,</p>
            <p style="color:#555;line-height:1.7;">
              Veuillez trouver ci-joint votre <strong>bulletin de paie</strong> pour la période indiquée ci-dessous.
            </p>

            <div style="background:#FEF5EC;border-left:4px solid #EDA832;padding:14px 20px;margin:20px 0;border-radius:0 6px 6px 0;">
              <p style="margin:0;font-size:11px;text-transform:uppercase;letter-spacing:1px;color:#C88500;font-weight:600;">Période</p>
              <p style="margin:6px 0 0;font-size:16px;font-weight:700;color:#1C0800;">%s</p>
            </div>

            <p style="color:#555;line-height:1.7;">
              Ce document est mis à disposition à titre informatif. Pour toute question
              concernant votre rémunération, veuillez contacter le service Finance de l'ISM.
            </p>
            """.formatted(nom, periode));
    }

    private String htmlReleveSoumis(String prenom, String module, String periode) {
        return wrap("""
            <p style="margin:0 0 8px;font-size:15px;color:#444;">Bonjour <strong style="color:#1C0800;">%s</strong>,</p>
            <p style="color:#555;line-height:1.7;">
              Votre <strong>relevé d'heures</strong> a bien été transmis et est en attente de traitement par le Relais Finance.
            </p>

            %s

            <p style="color:#555;line-height:1.7;">
              Vous serez notifié(e) par e-mail dès que votre relevé aura été traité.
              Vous pouvez également suivre son statut en temps réel sur la plateforme RHConnect.
            </p>

            <div style="text-align:center;margin:28px 0;">
              <a href="%s/dashboard" style="display:inline-block;background:#1C0800;color:#EDA832;text-decoration:none;padding:13px 32px;border-radius:6px;font-size:14px;font-weight:600;letter-spacing:0.5px;">
                Suivre mon relevé
              </a>
            </div>
            """.formatted(prenom, cartouche(module, periode, "En attente de validation", "#F59E0B"), frontendUrl));
    }

    private String htmlReleveValide(String prenom, String module, String periode, String totalHeures) {
        return wrap("""
            <p style="margin:0 0 8px;font-size:15px;color:#444;">Bonjour <strong style="color:#1C0800;">%s</strong>,</p>
            <p style="color:#555;line-height:1.7;">
              Nous avons le plaisir de vous informer que votre <strong>relevé d'heures</strong> a été
              <strong style="color:#16A34A;">validé</strong> par le Relais Finance.
            </p>

            %s

            <div style="background:#F0FDF4;border-left:4px solid #16A34A;padding:14px 20px;margin:20px 0;border-radius:0 6px 6px 0;">
              <p style="margin:0;font-size:11px;text-transform:uppercase;letter-spacing:1px;color:#15803D;font-weight:600;">Total des heures validées</p>
              <p style="margin:6px 0 0;font-size:20px;font-weight:700;color:#14532D;">%s</p>
            </div>

            <p style="color:#555;line-height:1.7;">
              Votre bulletin de paie correspondant sera généré et transmis prochainement.
            </p>
            """.formatted(prenom, cartouche(module, periode, "Validé", "#16A34A"), totalHeures));
    }

    private String htmlReleveRejete(String prenom, String module, String periode, String motif) {
        String blocMotif = (motif != null && !motif.isBlank())
            ? """
              <div style="background:#FFF7F7;border-left:4px solid #DC2626;padding:14px 20px;margin:20px 0;border-radius:0 6px 6px 0;">
                <p style="margin:0;font-size:11px;text-transform:uppercase;letter-spacing:1px;color:#DC2626;font-weight:600;">Motif communiqué</p>
                <p style="margin:6px 0 0;font-size:14px;color:#7F1D1D;line-height:1.6;">%s</p>
              </div>
              """.formatted(motif)
            : "";

        return wrap("""
            <p style="margin:0 0 8px;font-size:15px;color:#444;">Bonjour <strong style="color:#1C0800;">%s</strong>,</p>
            <p style="color:#555;line-height:1.7;">
              Votre <strong>relevé d'heures</strong> n'a pas pu être accepté en l'état.
              Il a été <strong style="color:#DC2626;">retourné</strong> pour révision par le Relais Finance.
            </p>

            %s
            %s

            <p style="color:#555;line-height:1.7;">
              Veuillez corriger les éléments signalés et soumettre à nouveau votre relevé
              depuis la plateforme RHConnect. En cas de difficulté, rapprochez-vous
              de votre Responsable de Programme.
            </p>

            <div style="text-align:center;margin:28px 0;">
              <a href="%s/dashboard" style="display:inline-block;background:#1C0800;color:#EDA832;text-decoration:none;padding:13px 32px;border-radius:6px;font-size:14px;font-weight:600;letter-spacing:0.5px;">
                Corriger mon relevé
              </a>
            </div>
            """.formatted(prenom, cartouche(module, periode, "Retourné pour révision", "#DC2626"), blocMotif, frontendUrl));
    }

    /* ═══════════════════════════════════════════════════════
       COMPOSANTS HTML RÉUTILISABLES
    ═══════════════════════════════════════════════════════ */

    private String wrap(String contenu) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
            <body style="margin:0;padding:0;background:#EFEFEF;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#EFEFEF;padding:32px 16px;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;">

                    <!-- EN-TÊTE -->
                    <tr>
                      <td style="background:#1C0800;padding:28px 36px;border-radius:10px 10px 0 0;">
                        <table width="100%%" cellpadding="0" cellspacing="0">
                          <tr>
                            <td>
                              <p style="margin:0;font-size:20px;font-weight:700;color:#EDA832;letter-spacing:0.5px;">RHConnect</p>
                              <p style="margin:3px 0 0;font-size:12px;color:rgba(255,255,255,0.45);letter-spacing:1.5px;text-transform:uppercase;">Institut Supérieur de Management · Dakar</p>
                            </td>
                            <td align="right">
                              <p style="margin:0;font-size:11px;color:rgba(255,255,255,0.3);">Portail Vacataires</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <!-- CORPS -->
                    <tr>
                      <td style="background:#ffffff;padding:36px 36px 28px;border-left:1px solid #E8E8E8;border-right:1px solid #E8E8E8;">
                        %s
                      </td>
                    </tr>

                    <!-- PIED DE PAGE -->
                    <tr>
                      <td style="background:#F7F7F7;padding:18px 36px;border:1px solid #E8E8E8;border-top:none;border-radius:0 0 10px 10px;">
                        <table width="100%%" cellpadding="0" cellspacing="0">
                          <tr>
                            <td>
                              <p style="margin:0;font-size:11px;color:#AAAAAA;line-height:1.6;">
                                Institut Supérieur de Management &mdash; Dakar, Sénégal<br>
                                Ce message est confidentiel et destiné exclusivement à son destinataire.
                              </p>
                            </td>
                            <td align="right" style="white-space:nowrap;">
                              <p style="margin:0;font-size:11px;color:#C88500;font-weight:600;">RHConnect</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(contenu);
    }

    private String badgeProfil(String role, String email) {
        return """
            <table style="width:100%%;border-collapse:collapse;margin:16px 0;">
              <tr>
                <td style="padding:10px 14px;background:#f5f5f5;border:1px solid #e0e0e0;font-size:13px;color:#777;width:140px;">Profil</td>
                <td style="padding:10px 14px;background:#fff;border:1px solid #e0e0e0;">
                  <span style="display:inline-block;background:#1C0800;color:#EDA832;font-size:11px;font-weight:700;padding:3px 10px;border-radius:20px;letter-spacing:0.5px;">%s</span>
                </td>
              </tr>
              <tr>
                <td style="padding:10px 14px;background:#f5f5f5;border:1px solid #e0e0e0;border-top:none;font-size:13px;color:#777;">Email</td>
                <td style="padding:10px 14px;background:#fff;border:1px solid #e0e0e0;border-top:none;font-size:13px;color:#444;">%s</td>
              </tr>
            </table>
            """.formatted(role, email);
    }

    private String cartouche(String module, String periode, String statut, String couleurStatut) {
        return """
            <table style="width:100%%;border-collapse:collapse;margin:16px 0;border-radius:8px;overflow:hidden;">
              <tr style="background:#F9F9F9;">
                <td style="padding:10px 16px;border:1px solid #E8E8E8;font-size:12px;text-transform:uppercase;letter-spacing:0.8px;color:#888;width:130px;">Module</td>
                <td style="padding:10px 16px;border:1px solid #E8E8E8;border-left:none;font-size:14px;font-weight:600;color:#1C0800;">%s</td>
              </tr>
              <tr style="background:#FFFFFF;">
                <td style="padding:10px 16px;border:1px solid #E8E8E8;border-top:none;font-size:12px;text-transform:uppercase;letter-spacing:0.8px;color:#888;">Période</td>
                <td style="padding:10px 16px;border:1px solid #E8E8E8;border-top:none;border-left:none;font-size:14px;color:#444;">%s</td>
              </tr>
              <tr style="background:#F9F9F9;">
                <td style="padding:10px 16px;border:1px solid #E8E8E8;border-top:none;font-size:12px;text-transform:uppercase;letter-spacing:0.8px;color:#888;">Statut</td>
                <td style="padding:10px 16px;border:1px solid #E8E8E8;border-top:none;border-left:none;">
                  <span style="display:inline-block;background:%s;color:#fff;font-size:11px;font-weight:700;padding:3px 10px;border-radius:20px;">%s</span>
                </td>
              </tr>
            </table>
            """.formatted(module, periode, couleurStatut, statut);
    }
}
