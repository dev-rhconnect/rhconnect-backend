package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.TypeVacataire;
import com.ism.rhconnect.entity.Vacataire;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class VacataireResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String specialite;
    private List<String> specialites;
    private List<String> niveaux;
    private List<String> modules;
    private String telephone;
    private String adresse;
    private String situationMatrimoniale;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String nationalite;
    private String numeroCni;
    private String ninea;
    private String ipres;
    private String nomBanque;
    private String rib;
    private TypeVacataire typeVacataire;
    private Vacataire.StatutVacataire statut;
    private boolean signatureUploaded;
    private boolean aContratActif;
    private boolean profilComplet;
}
