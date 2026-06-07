-- Compte Admin IT — Mame Coumba SALL
-- Email : mame-coumba.sall@ism.edu.sn
-- Mot de passe : Admin@ISM2026
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
VALUES (
    'Sall',
    'Mame Coumba',
    'mame-coumba.sall@ism.edu.sn',
    '$2b$12$r/wnHN6ICVJzlYYli15Yv.OWU.euQEv9bz7dptywtYS6gxe7ovvsG',
    'ADMIN',
    true,
    false,
    NOW()
) ON CONFLICT (email) DO NOTHING;
