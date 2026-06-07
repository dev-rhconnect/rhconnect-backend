package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private boolean premierConnexion;
}
