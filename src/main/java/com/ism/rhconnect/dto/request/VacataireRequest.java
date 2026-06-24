package com.ism.rhconnect.dto.request;

import com.ism.rhconnect.entity.TypeVacataire;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class VacataireRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    private String telephone;
    private String adresse;
    private String situationMatrimoniale;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String nationalite;
    private String numeroCni;
    private String ninea;
    private String ipres;

    // Coordonnées bancaires
    private String nomBanque;
    private String codeBanque;
    private String codeGuichet;
    private String numeroCompte;
    private String rib;

    // Profil pédagogique
    private TypeVacataire typeVacataire;
    private List<String> specialites;
    private List<String> niveaux;
    private List<String> modules;
}
