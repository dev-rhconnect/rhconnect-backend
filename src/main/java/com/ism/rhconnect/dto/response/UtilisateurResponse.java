package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UtilisateurResponse {
    private Long id;
    private String prenom;
    private String nom;
    private String email;
    private Role role;
    private boolean actif;
    private LocalDateTime dateCreation;
}
