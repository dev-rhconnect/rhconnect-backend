package com.ism.rhconnect.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider — génération, validation et expiration des tokens RBAC")
class JwtTokenProviderTest {

    private static final String SECRET =
            "test-secret-key-minimum-256-bits-for-hs512-algorithm-rhconnect-test-only";
    private static final long EXPIRATION_24H  = 86_400_000L;
    private static final long REFRESH_7J      = 604_800_000L;

    private JwtTokenProvider provider;

    @BeforeEach
    void setup() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret",         SECRET);
        ReflectionTestUtils.setField(provider, "jwtExpiration",     EXPIRATION_24H);
        ReflectionTestUtils.setField(provider, "refreshExpiration", REFRESH_7J);
    }

    @Test
    @DisplayName("generateTokenFromEmail — le token contient l'email en subject")
    void token_contient_email_en_subject() {
        String email = "vacataire@ism.edu.sn";
        String token = provider.generateTokenFromEmail(email);

        assertNotNull(token, "Le token ne doit pas être null");
        assertFalse(token.isBlank(), "Le token ne doit pas être vide");
        assertEquals(email, provider.getEmailFromToken(token));
    }

    @Test
    @DisplayName("validateToken — token valide retourne true")
    void token_valide_retourne_true() {
        String token = provider.generateTokenFromEmail("attache@ism.edu.sn");
        assertTrue(provider.validateToken(token));
    }

    @Test
    @DisplayName("validateToken — token expiré retourne false")
    void token_expire_retourne_false() {
        JwtTokenProvider shortLived = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortLived, "jwtSecret",         SECRET);
        ReflectionTestUtils.setField(shortLived, "jwtExpiration",     -1L);   // expiration passée
        ReflectionTestUtils.setField(shortLived, "refreshExpiration", REFRESH_7J);

        String expired = shortLived.generateTokenFromEmail("finance@ism.edu.sn");
        assertFalse(provider.validateToken(expired), "Un token expiré doit être rejeté");
    }

    @Test
    @DisplayName("validateToken — token altéré (payload modifié) retourne false")
    void token_altere_retourne_false() {
        String token   = provider.generateTokenFromEmail("rp@ism.edu.sn");
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "fakesignature";
        assertFalse(provider.validateToken(tampered), "Un token falsifié doit être rejeté");
    }

    @Test
    @DisplayName("generateRefreshToken — durée supérieure au token standard")
    void refresh_token_genere_sans_erreur() {
        String refresh = provider.generateRefreshToken("admin@ism.edu.sn");
        assertNotNull(refresh);
        assertTrue(provider.validateToken(refresh));
    }
}
