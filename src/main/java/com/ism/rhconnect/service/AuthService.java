package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.ChangePasswordRequest;
import com.ism.rhconnect.dto.request.LoginRequest;
import com.ism.rhconnect.dto.response.AuthResponse;
import com.ism.rhconnect.entity.Utilisateur;
import com.ism.rhconnect.exception.UnauthorizedException;
import com.ism.rhconnect.repository.UtilisateurRepository;
import com.ism.rhconnect.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("Utilisateur introuvable"));

            String token        = jwtTokenProvider.generateTokenFromEmail(utilisateur.getEmail());
            String refreshToken = jwtTokenProvider.generateRefreshToken(utilisateur.getEmail());

            return AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .email(utilisateur.getEmail())
                    .nom(utilisateur.getNom())
                    .prenom(utilisateur.getPrenom())
                    .role(utilisateur.getRole())
                    .premierConnexion(utilisateur.isPremierConnexion())
                    .build();

        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        } catch (DisabledException e) {
            throw new UnauthorizedException("Votre compte est inactif. Contactez l'administrateur");
        }
    }

    public Utilisateur getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Utilisateur introuvable"));
    }

    @Transactional
    public void changerMotDePasse(ChangePasswordRequest request) {
        Utilisateur user = getCurrentUser();
        if (!passwordEncoder.matches(request.getAncienMotDePasse(), user.getMotDePasse())) {
            throw new UnauthorizedException("Ancien mot de passe incorrect");
        }
        user.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        user.setPremierConnexion(false);
        utilisateurRepository.save(user);
    }
}
