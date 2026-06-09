package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.Vacataire;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VacataireResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String specialite;
    private String telephone;
    private String adresse;
    private String numeroCni;
    private String ninea;
    private String ipres;
    private String nomBanque;
    private String rib;
    private Vacataire.StatutVacataire statut;
    private boolean signatureUploaded;
    private boolean aContratActif;
    private boolean profilComplet;  // signatureUploaded && aContratActif
}
