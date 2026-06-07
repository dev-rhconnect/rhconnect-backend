package com.ism.rhconnect.dto.request;

import com.ism.rhconnect.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    /** Mot de passe temporaire défini par l'admin. Si absent, un mot de passe par défaut est généré. */
    private String motDePasseTemporaire;
}
