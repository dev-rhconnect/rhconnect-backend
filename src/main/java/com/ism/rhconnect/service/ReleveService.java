package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.FeuilleHeureRequest;
import com.ism.rhconnect.dto.request.LigneHeureRequest;
import com.ism.rhconnect.dto.response.FeuilleHeureResponse;
import com.ism.rhconnect.dto.response.LigneHeureResponse;
import com.ism.rhconnect.entity.*;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.exception.UnauthorizedException;
import com.ism.rhconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReleveService {

    private final FeuilleHeureRepository feuilleHeureRepository;
    private final LigneHeureRepository ligneHeureRepository;
    private final ContratRepository contratRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final PaiementRepository paiementRepository;
    private final EmailService emailService;
    private final SeanceProgrammeeRepository seanceProgrammeeRepository;

    /* ══════════════════════════════════════════════════════════════
       AUTO-CREATION — appelé par SeanceProgrammeeService.valider()
       ══════════════════════════════════════════════════════════════ */

    /**
     * Crée ou incrémente le relevé mensuel correspondant à la séance validée.
     * Le relevé est identifié par : (ContratModule + mois) ou (Contrat + mois) pour les anciens contrats.
     */
    @Transactional
    public void creerOuIncrementer(SeanceProgrammee seance, Utilisateur attache) {
        String periode = YearMonth.from(seance.getDateSeance()).toString(); // "2026-06"
        ContratModule cm = seance.getContratModule();

        // Trouver ou créer la feuille EN_COURS pour ce mois
        FeuilleHeure feuille;
        if (cm != null) {
            feuille = feuilleHeureRepository
                    .findByContratModuleIdAndPeriode(cm.getId(), periode)
                    .orElseGet(() -> creerNouveauReleve(seance.getContrat(), cm, attache, periode));
        } else {
            feuille = feuilleHeureRepository
                    .findByContratIdAndPeriodeAndContratModuleIsNull(seance.getContrat().getId(), periode)
                    .orElseGet(() -> creerNouveauReleve(seance.getContrat(), null, attache, periode));
        }

        // Si le relevé n'est plus EN_COURS (soumis/validé), ne pas ajouter la ligne
        if (feuille.getStatut() != FeuilleHeure.Statut.EN_COURS) {
            return;
        }

        // Anti-doublon : vérifier que cette séance n'a pas déjà été ajoutée
        if (ligneHeureRepository.existsByFeuilleHeureIdAndSeanceProgrammeeId(feuille.getId(), seance.getId())) {
            return;
        }

        // Ajouter la ligne
        LigneHeure ligne = LigneHeure.builder()
                .feuilleHeure(feuille)
                .date(seance.getDateSeance())
                .heureDebut(seance.getHeureDebut())
                .heureFin(seance.getHeureFin())
                .duree(seance.getDuree())
                .seanceProgrammeeId(seance.getId())
                .statut(LigneHeure.Statut.SAISIE)
                .build();
        ligneHeureRepository.save(ligne);

        // Mettre à jour le total
        feuille.setTotalHeuresValidees(feuille.getTotalHeuresValidees() + seance.getDuree());
        feuilleHeureRepository.save(feuille);
    }

    private FeuilleHeure creerNouveauReleve(Contrat contrat, ContratModule cm, Utilisateur attache, String periode) {
        return feuilleHeureRepository.save(
                FeuilleHeure.builder()
                        .contrat(contrat)
                        .contratModule(cm)
                        .attache(attache)
                        .periode(periode)
                        .statut(FeuilleHeure.Statut.EN_COURS)
                        .totalHeuresValidees(0.0)
                        .build());
    }

    /* ══════════════════════════════════════════════════════
       LECTURE avec filtres
       ══════════════════════════════════════════════════════ */

    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> lister(String periode, String classeNom, Long vacataireId) {
        Utilisateur appelant = getUtilisateurConnecte();

        List<FeuilleHeure> feuilles;
        if (appelant.getRole() == Role.ATTACHE_CLASSE) {
            feuilles = feuilleHeureRepository.findByAttacheId(appelant.getId());
        } else {
            feuilles = feuilleHeureRepository.findAll();
        }

        Stream<FeuilleHeure> stream = feuilles.stream();

        if (periode != null && !periode.isBlank()) {
            stream = stream.filter(f -> f.getPeriode().equals(periode));
        }
        if (vacataireId != null) {
            stream = stream.filter(f -> f.getContrat().getVacataire().getId().equals(vacataireId));
        }
        if (classeNom != null && !classeNom.isBlank()) {
            stream = stream.filter(f -> feuilleAppartientAClasse(f, classeNom));
        }

        return stream.map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> listerSoumisRP() {
        return feuilleHeureRepository.findByStatutIn(
                List.of(FeuilleHeure.Statut.SOUMIS_RP, FeuilleHeure.Statut.SOUMIS,
                        FeuilleHeure.Statut.VALIDE_RP, FeuilleHeure.Statut.SOUMIS_FINANCE,
                        FeuilleHeure.Statut.VALIDE, FeuilleHeure.Statut.REJETE))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> listerSoumisFinance() {
        return feuilleHeureRepository.findByStatutIn(
                List.of(FeuilleHeure.Statut.SOUMIS_FINANCE,
                        FeuilleHeure.Statut.VALIDE,
                        FeuilleHeure.Statut.REJETE))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> mesMesReleves() {
        Utilisateur u = getUtilisateurConnecte();
        return feuilleHeureRepository.findByAttacheId(u.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> mesRelevesValides() {
        Utilisateur vacataire = getUtilisateurConnecte();
        return feuilleHeureRepository
                .findByContratVacataireUtilisateurId(vacataire.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeuilleHeureResponse> listerEquipeRP() {
        return feuilleHeureRepository.findAll()
                .stream()
                .filter(f -> f.getStatut() != FeuilleHeure.Statut.EN_COURS)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeuilleHeureResponse trouverParId(Long id) {
        return toResponse(findOrThrow(id));
    }

    /* ══════════════════════════════════════════════════════
       WORKFLOW : Attaché → RP → Finance
       ══════════════════════════════════════════════════════ */

    /** Attaché soumet le relevé au RP. */
    @Transactional
    public FeuilleHeureResponse soumettre(Long id) {
        FeuilleHeure feuille = findOrThrow(id);

        if (feuille.getStatut() != FeuilleHeure.Statut.EN_COURS) {
            throw new IllegalStateException("Ce relevé ne peut pas être soumis (statut : " + feuille.getStatut() + ")");
        }
        if (feuille.getLignes() == null || feuille.getLignes().isEmpty()) {
            throw new IllegalStateException("Impossible de soumettre un relevé sans séances");
        }

        feuille.setStatut(FeuilleHeure.Statut.SOUMIS_RP);
        feuille.setDateSoumission(LocalDateTime.now());
        FeuilleHeure saved = feuilleHeureRepository.save(feuille);

        // Notifier les RP
        List<Utilisateur> responsables = utilisateurRepository.findByRole(Role.RESPONSABLE_PROGRAMME);
        String nomVacataire = feuille.getContrat().getVacataire().getUtilisateur().getPrenom()
                + " " + feuille.getContrat().getVacataire().getUtilisateur().getNom();
        String nomModule = nomModule(feuille);
        for (Utilisateur rp : responsables) {
            notificationService.creer(rp, Notification.Type.RELEVE_SOUMIS,
                    "Relevé de " + feuille.getPeriode() + " — " + nomVacataire
                            + " (" + nomModule + ") soumis par "
                            + feuille.getAttache().getPrenom() + " " + feuille.getAttache().getNom()
                            + ". En attente de validation.");
        }

        return toResponse(saved);
    }

    /** RP valide le relevé soumis par l'attaché. */
    @Transactional
    public FeuilleHeureResponse validerParRP(Long id) {
        FeuilleHeure feuille = findOrThrow(id);

        boolean estSoumis = feuille.getStatut() == FeuilleHeure.Statut.SOUMIS_RP
                || feuille.getStatut() == FeuilleHeure.Statut.SOUMIS; // backward compat
        if (!estSoumis) {
            throw new IllegalStateException("Ce relevé doit être soumis au RP avant validation (statut : " + feuille.getStatut() + ")");
        }

        feuille.setStatut(FeuilleHeure.Statut.VALIDE_RP);
        FeuilleHeure saved = feuilleHeureRepository.save(feuille);

        notificationService.creer(feuille.getAttache(), Notification.Type.RELEVE_VALIDE,
                "Votre relevé de " + feuille.getPeriode() + " (" + nomModule(feuille)
                        + ") a été validé par le Responsable de Programme.");

        return toResponse(saved);
    }

    /** RP transmet le relevé validé au Relais Finance. */
    @Transactional
    public FeuilleHeureResponse soumettreAFinance(Long id) {
        FeuilleHeure feuille = findOrThrow(id);

        if (feuille.getStatut() != FeuilleHeure.Statut.VALIDE_RP) {
            throw new IllegalStateException("Le relevé doit être validé par le RP avant transmission (statut : " + feuille.getStatut() + ")");
        }

        feuille.setStatut(FeuilleHeure.Statut.SOUMIS_FINANCE);
        FeuilleHeure saved = feuilleHeureRepository.save(feuille);

        List<Utilisateur> finances = utilisateurRepository.findByRole(Role.RELAIS_FINANCE);
        String nomVacataire = feuille.getContrat().getVacataire().getUtilisateur().getPrenom()
                + " " + feuille.getContrat().getVacataire().getUtilisateur().getNom();
        for (Utilisateur finance : finances) {
            notificationService.creer(finance, Notification.Type.RELEVE_SOUMIS,
                    "Relevé de " + nomVacataire + " — " + feuille.getPeriode()
                            + " (" + nomModule(feuille) + ") transmis pour paiement.");
        }

        return toResponse(saved);
    }

    /** Relais Finance valide et génère le paiement. */
    @Transactional
    public FeuilleHeureResponse valider(Long id) {
        FeuilleHeure feuille = findOrThrow(id);

        if (feuille.getStatut() != FeuilleHeure.Statut.SOUMIS_FINANCE) {
            throw new IllegalStateException("Ce relevé doit être transmis au Finance avant validation (statut : " + feuille.getStatut() + ")");
        }

        double total = feuille.getLignes() != null
                ? feuille.getLignes().stream()
                    .filter(l -> l.getStatut() != LigneHeure.Statut.REJETEE)
                    .mapToDouble(l -> l.getDuree() != null ? l.getDuree() : 0).sum()
                : 0;

        feuille.setTotalHeuresValidees(total);
        feuille.setStatut(FeuilleHeure.Statut.VALIDE);
        feuille.setDateValidation(LocalDateTime.now());
        feuille.setMotifRejet(null);
        FeuilleHeure saved = feuilleHeureRepository.save(feuille);

        notificationService.creer(feuille.getAttache(), Notification.Type.RELEVE_VALIDE,
                "Votre relevé de " + feuille.getPeriode() + " (" + nomModule(feuille)
                        + ") a été validé par le Relais Finance. Total : " + String.format("%.1f h", total));

        Utilisateur vacataire = feuille.getContrat().getVacataire().getUtilisateur();
        Double tauxHoraire = feuille.getContrat().getTauxHoraire();
        if (tauxHoraire != null && tauxHoraire > 0 && total > 0) {
            double montantBrut    = total * tauxHoraire;
            double retenueFiscale = montantBrut * 0.05;
            double montantNet     = montantBrut - retenueFiscale;
            paiementRepository.save(Paiement.builder()
                    .feuilleHeure(saved)
                    .totalHeures(total)
                    .tauxHoraire(tauxHoraire)
                    .montantBrut(montantBrut)
                    .retenueFiscale(retenueFiscale)
                    .montantNet(montantNet)
                    .build());
            notificationService.creer(vacataire, Notification.Type.FICHE_PAIE_DISPONIBLE,
                    "Votre fiche de paie pour " + feuille.getPeriode()
                            + " (" + nomModule(feuille) + ") est disponible."
                            + " Net à payer : " + String.format("%.0f FCFA", montantNet));
        }

        return toResponse(saved);
    }

    /** Finance ou RP rejette un relevé soumis. */
    @Transactional
    public FeuilleHeureResponse rejeter(Long id, String motif) {
        FeuilleHeure feuille = findOrThrow(id);

        boolean rejectable = feuille.getStatut() == FeuilleHeure.Statut.SOUMIS_FINANCE
                || feuille.getStatut() == FeuilleHeure.Statut.SOUMIS_RP
                || feuille.getStatut() == FeuilleHeure.Statut.SOUMIS;
        if (!rejectable) {
            throw new IllegalStateException("Ce relevé ne peut pas être rejeté (statut : " + feuille.getStatut() + ")");
        }

        feuille.setStatut(FeuilleHeure.Statut.REJETE);
        feuille.setMotifRejet(motif);
        FeuilleHeure saved = feuilleHeureRepository.save(feuille);

        String msgMotif = (motif != null && !motif.isBlank()) ? " Motif : " + motif : "";
        notificationService.creer(feuille.getAttache(), Notification.Type.RELEVE_REJETE,
                "Votre relevé de " + feuille.getPeriode() + " (" + nomModule(feuille)
                        + ") a été rejeté." + msgMotif);

        List<Utilisateur> responsables = utilisateurRepository.findByRole(Role.RESPONSABLE_PROGRAMME);
        for (Utilisateur rp : responsables) {
            notificationService.creer(rp, Notification.Type.DEMANDE_EXPLICATION,
                    "Relevé de " + feuille.getAttache().getPrenom() + " " + feuille.getAttache().getNom()
                            + " (" + feuille.getPeriode() + " — " + nomModule(feuille) + ") rejeté." + msgMotif);
        }

        return toResponse(saved);
    }

    @Transactional
    public FeuilleHeureResponse repondreExplication(Long id, String reponse) {
        FeuilleHeure feuille = findOrThrow(id);
        if (feuille.getStatut() != FeuilleHeure.Statut.REJETE) {
            throw new IllegalStateException("Ce relevé n'est pas en statut rejeté");
        }
        Utilisateur rp = getUtilisateurConnecte();
        List<Utilisateur> finances = utilisateurRepository.findByRole(Role.RELAIS_FINANCE);
        for (Utilisateur finance : finances) {
            notificationService.creer(finance, Notification.Type.REPONSE_EXPLICATION,
                    rp.getPrenom() + " " + rp.getNom() + " a répondu concernant le relevé de "
                            + feuille.getAttache().getPrenom() + " " + feuille.getAttache().getNom()
                            + " (" + feuille.getPeriode() + ") : " + reponse);
        }
        return toResponse(feuille);
    }

    /* ══════════════════════════════════════════════════════
       Ajout manuel d'une ligne (gardé pour compatibilité)
       ══════════════════════════════════════════════════════ */

    @Transactional
    public LigneHeureResponse ajouterLigne(Long feuilleId, LigneHeureRequest request) {
        FeuilleHeure feuille = findOrThrow(feuilleId);
        if (feuille.getStatut() != FeuilleHeure.Statut.EN_COURS) {
            throw new IllegalStateException("Impossible d'ajouter une séance : le relevé est " + feuille.getStatut());
        }
        double duree = calculerDuree(request);
        LigneHeure ligne = LigneHeure.builder()
                .feuilleHeure(feuille)
                .date(request.getDate())
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .duree(duree)
                .observation(request.isAbsence()
                        ? "ABSENCE" + (request.getObservation() != null ? " — " + request.getObservation() : "")
                        : request.getObservation())
                .statut(request.isAbsence() ? LigneHeure.Statut.REJETEE : LigneHeure.Statut.SAISIE)
                .build();
        LigneHeure saved = ligneHeureRepository.save(ligne);
        feuille.setTotalHeuresValidees(feuille.getTotalHeuresValidees() + duree);
        feuilleHeureRepository.save(feuille);
        return toLigneResponse(saved, feuilleId);
    }

    /* ══════════════════════════════════════════════════════
       Helpers
       ══════════════════════════════════════════════════════ */

    private boolean feuilleAppartientAClasse(FeuilleHeure f, String classeNom) {
        ContratModule cm = f.getContratModule();
        if (cm != null && cm.getClasses() != null) {
            return cm.getClasses().contains(classeNom);
        }
        String ancienneClasse = f.getContrat().getClasse();
        return classeNom.equals(ancienneClasse);
    }

    private String nomModule(FeuilleHeure f) {
        ContratModule cm = f.getContratModule();
        if (cm != null) return cm.getNomModule();
        String m = f.getContrat().getModule();
        return m != null ? m : "—";
    }

    private FeuilleHeure findOrThrow(Long id) {
        return feuilleHeureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relevé introuvable : " + id));
    }

    private Utilisateur getUtilisateurConnecte() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Utilisateur introuvable"));
    }

    private double calculerDuree(LigneHeureRequest req) {
        if (req.isAbsence()) return 0.0;
        long minutes = req.getHeureDebut().until(req.getHeureFin(), java.time.temporal.ChronoUnit.MINUTES);
        return minutes / 60.0;
    }

    private FeuilleHeureResponse toResponse(FeuilleHeure f) {
        ContratModule cm = f.getContratModule();
        String nomModule = cm != null ? cm.getNomModule() : f.getContrat().getModule();
        List<String> classes = cm != null ? cm.getClasses() : null;
        String classeAffichage = classes != null && !classes.isEmpty()
                ? String.join(", ", classes)
                : f.getContrat().getClasse();

        List<LigneHeureResponse> lignes = (f.getLignes() != null)
                ? f.getLignes().stream().map(l -> toLigneResponse(l, f.getId())).collect(Collectors.toList())
                : List.of();

        Utilisateur vacataire = f.getContrat().getVacataire().getUtilisateur();

        return FeuilleHeureResponse.builder()
                .id(f.getId())
                .contratId(f.getContrat().getId())
                .contratModuleId(cm != null ? cm.getId() : null)
                .vacataireId(f.getContrat().getVacataire().getId())
                .nomVacataire(vacataire.getPrenom() + " " + vacataire.getNom())
                .module(nomModule)
                .classe(classeAffichage)
                .classes(classes)
                .periode(f.getPeriode())
                .nombreSeances(lignes.size())
                .totalHeuresValidees(f.getTotalHeuresValidees())
                .volumeHorairePrevisionnel(f.getContrat().getVolumeHorairePrevisionnel())
                .tauxHoraire(f.getContrat().getTauxHoraire())
                .statut(f.getStatut())
                .dateSoumission(f.getDateSoumission())
                .dateValidation(f.getDateValidation())
                .motifRejet(f.getMotifRejet())
                .lignes(lignes)
                .build();
    }

    private LigneHeureResponse toLigneResponse(LigneHeure l, Long feuilleId) {
        return LigneHeureResponse.builder()
                .id(l.getId())
                .feuilleHeureId(feuilleId)
                .date(l.getDate())
                .heureDebut(l.getHeureDebut())
                .heureFin(l.getHeureFin())
                .duree(l.getDuree())
                .observation(l.getObservation())
                .statut(l.getStatut())
                .build();
    }
}
