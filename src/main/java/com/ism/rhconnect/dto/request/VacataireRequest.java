package com.ism.rhconnect.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VacataireRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "La spécialité est obligatoire")
    private String specialite;

    private String telephone;
    private String adresse;
    private String situationMatrimoniale;
    private String numeroCni;
    private String ninea;
    private String ipres;

    // Coordonnées bancaires
    private String nomBanque;
    private String codeBanque;
    private String codeGuichet;
    private String numeroCompte;
    private String rib;
}
