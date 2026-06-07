package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.request.ChangePasswordRequest;
import com.ism.rhconnect.dto.request.LoginRequest;
import com.ism.rhconnect.dto.response.AuthResponse;
import com.ism.rhconnect.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    /** Changer son propre mot de passe (premier connexion ou reset). */
    @PutMapping("/change-password")
    public ResponseEntity<Void> changerMotDePasse(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changerMotDePasse(request);
        return ResponseEntity.noContent().build();
    }
}
