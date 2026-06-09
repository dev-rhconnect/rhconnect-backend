package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.VacataireRequest;
import com.ism.rhconnect.dto.response.VacataireResponse;
import com.ism.rhconnect.entity.Contrat;
import com.ism.rhconnect.entity.Role;
import com.ism.rhconnect.entity.Utilisateur;
import com.ism.rhconnect.entity.Vacataire;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.repository.ContratRepository;
import com.ism.rhconnect.repository.UtilisateurRepository;
import com.ism.rhconnect.repository.VacataireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VacataireService {

    private final VacataireRepository vacataireRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ContratRepository contratRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public VacataireResponse creer(VacataireRequest request) {
        // Créer le compte utilisateur du vacataire
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode("Vacataire@ISM2026"))
                .role(Role.VACATAIRE)
                .build();
        utilisateurRepository.save(utilisateur);

        // Créer le dossier vacataire
        Vacataire vacataire = Vacataire.builder()
                .utilisateur(utilisateur)
                .specialite(request.getSpecialite())
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .situationMatrimoniale(request.getSituationMatrimoniale())
                .numeroCni(request.getNumeroCni())
                .ninea(request.getNinea())
                .ipres(request.getIpres())
                .nomBanque(request.getNomBanque())
                .codeBanque(request.getCodeBanque())
                .codeGuichet(request.getCodeGuichet())
                .numeroCompte(request.getNumeroCompte())
                .rib(request.getRib())
                .build();

        return toResponse(vacataireRepository.save(vacataire));
    }

    public List<VacataireResponse> listerTous() {
        return vacataireRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public VacataireResponse trouverParId(Long id) {
        return toResponse(vacataireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacataire introuvable : " + id)));
    }

    @Transactional
    public VacataireResponse modifier(Long id, VacataireRequest request) {
        Vacataire vacataire = vacataireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacataire introuvable : " + id));

        vacataire.setSpecialite(request.getSpecialite());
        vacataire.setTelephone(request.getTelephone());
        vacataire.setAdresse(request.getAdresse());
        vacataire.setNumeroCni(request.getNumeroCni());
        vacataire.setNinea(request.getNinea());
        vacataire.setIpres(request.getIpres());
        vacataire.setNomBanque(request.getNomBanque());
        vacataire.setCodeBanque(request.getCodeBanque());
        vacataire.setCodeGuichet(request.getCodeGuichet());
        vacataire.setNumeroCompte(request.getNumeroCompte());
        vacataire.setRib(request.getRib());

        return toResponse(vacataireRepository.save(vacataire));
    }

    @Transactional
    public void archiver(Long id) {
        Vacataire vacataire = vacataireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacataire introuvable : " + id));
        vacataire.setStatut(Vacataire.StatutVacataire.INACTIF);
        vacataireRepository.save(vacataire);
    }

    /** Sprint 2 — RP : uploader la signature électronique du vacataire. */
    @Transactional
    public VacataireResponse uploadSignature(Long id, MultipartFile file) throws IOException {
        Vacataire vacataire = vacataireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacataire introuvable : " + id));

        String originalName = file.getOriginalFilename();
        String extension = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : ".png";

        Path dir = Paths.get(uploadDir, "signatures");
        Files.createDirectories(dir);
        Path dest = dir.resolve("signature_vacataire_" + id + extension);
        Files.write(dest, file.getBytes());

        vacataire.setCheminSignature(dest.toString());
        return toResponse(vacataireRepository.save(vacataire));
    }

    /** Sprint 2 — Vacataire : mettre à jour ses propres coordonnées bancaires. */
    @Transactional
    public VacataireResponse mettreAJourCoordonnees(VacataireRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Vacataire vacataire = vacataireRepository.findByUtilisateurId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier vacataire introuvable"));

        vacataire.setTelephone(request.getTelephone());
        vacataire.setAdresse(request.getAdresse());
        vacataire.setNomBanque(request.getNomBanque());
        vacataire.setCodeBanque(request.getCodeBanque());
        vacataire.setCodeGuichet(request.getCodeGuichet());
        vacataire.setNumeroCompte(request.getNumeroCompte());
        vacataire.setRib(request.getRib());

        return toResponse(vacataireRepository.save(vacataire));
    }

    /** Sprint 2 — Vacataire : consulter son propre dossier. */
    @Transactional(readOnly = true)
    public VacataireResponse monDossier() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Vacataire vacataire = vacataireRepository.findByUtilisateurId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier vacataire introuvable"));
        return toResponse(vacataire);
    }

    private VacataireResponse toResponse(Vacataire v) {
        boolean signatureOk = v.getCheminSignature() != null;
        boolean contratActif = contratRepository.existsByVacataireIdAndStatut(
                v.getId(), Contrat.StatutContrat.ACTIF);
        return VacataireResponse.builder()
                .id(v.getId())
                .nom(v.getUtilisateur().getNom())
                .prenom(v.getUtilisateur().getPrenom())
                .email(v.getUtilisateur().getEmail())
                .specialite(v.getSpecialite())
                .telephone(v.getTelephone())
                .adresse(v.getAdresse())
                .numeroCni(v.getNumeroCni())
                .ninea(v.getNinea())
                .ipres(v.getIpres())
                .nomBanque(v.getNomBanque())
                .rib(v.getRib())
                .statut(v.getStatut())
                .signatureUploaded(signatureOk)
                .aContratActif(contratActif)
                .profilComplet(signatureOk && contratActif)
                .build();
    }
}
