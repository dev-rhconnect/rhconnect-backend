-- Compte Admin IT initial
-- Mot de passe : Admin@ISM2025 (hashé avec BCrypt facteur 12)
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
VALUES (
    'Admin',
    'IT',
    'admin@ism.sn',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'ADMIN',
    true,
    false,
    NOW()
) ON CONFLICT (email) DO NOTHING;
