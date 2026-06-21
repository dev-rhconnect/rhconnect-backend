-- =============================================================
-- ISM Dakar — Vacataires 2025-2026
-- Mot de passe commun : mariediagne
-- Hash bcrypt : $2b$10$j3R.wdYHLwgqZnbEc7ITiuw7fMqH.0i2V.GDqJJf8beatn5YPStie
-- Période : 01/09/2025 → 30/09/2026   Année : 2025-2026
-- Règle : 3 modules max dans le contrat principal, le reste en avenant
-- =============================================================

DO $$
DECLARE
    MDK   TEXT := '$2b$10$j3R.wdYHLwgqZnbEc7ITiuw7fMqH.0i2V.GDqJJf8beatn5YPStie';
    u_id  BIGINT;
    v_id  BIGINT;
    c_id  BIGINT;
    c2_id BIGINT;
    cm_id BIGINT;
BEGIN

-- =============================================================
-- 1. DJIBEIROU TALL — Anglais (6 modules → 3 + avenant 3)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Tall', 'Djibeirou', 'djibeirou.tall@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Anglais', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'English For IT 1', 'LICENCE', 120.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 ETSE'),(cm_id,'L3 MAE'),(cm_id,'L3 MOSIEF'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'English For IT 2', 'LICENCE', 120.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 ETSE'),(cm_id,'L3 MAE'),(cm_id,'L3 MOSIEF'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Business English 3', 'LICENCE', 80.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 GLRS A'),(cm_id,'L2 GLRS B'),(cm_id,'L2 ETSE'),(cm_id,'L2 TC');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Business English 4', 'LICENCE', 80.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 GLRS A'),(cm_id,'L2 GLRS B'),(cm_id,'L2 ETSE'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Business English 1', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1F'),(cm_id,'L1H');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Business English 2', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1F'),(cm_id,'L1H');

-- =============================================================
-- 2. MOUSTAPHA TOURE — Anglais (6 modules → 3 + avenant 3)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Toure', 'Moustapha', 'moustapha.toure@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Anglais', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'English For IT 1', 'LICENCE', 100.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B'),(cm_id,'L3 TTL A'),(cm_id,'L3 TTL B'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'English For IT 2', 'LICENCE', 100.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B'),(cm_id,'L3 TTL A'),(cm_id,'L3 TTL B'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Business English 3', 'LICENCE', 100.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 IA'),(cm_id,'L2 CS'),(cm_id,'L2 MAE'),(cm_id,'L2 MOSIEF'),(cm_id,'L2 TC');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Business English 4', 'LICENCE', 100.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 IA'),(cm_id,'L2 CS'),(cm_id,'L2 MAE'),(cm_id,'L2 MOSIEF'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Business English 1', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 CPD'),(cm_id,'L1 CDSD'),(cm_id,'L1 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Business English 2', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 CPD'),(cm_id,'L1 CDSD'),(cm_id,'L1 TC');

-- =============================================================
-- 3. MAGUETTE BA — Anglais (6 modules → 3 + avenant 3)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Ba', 'Maguette', 'maguette.ba@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Anglais', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'English For IT 1', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 CPD'),(cm_id,'L3 CDSD'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'English For IT 2', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 CPD'),(cm_id,'L3 CDSD'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Business English 3', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 CPD'),(cm_id,'L2 CDSD'),(cm_id,'L2 TC');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Business English 4', 'LICENCE', 100.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 CPD'),(cm_id,'L2 CDSD'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Business English 1', 'LICENCE', 80.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 IA'),(cm_id,'L1 CS'),(cm_id,'L1 G'),(cm_id,'L1 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Business English 2', 'LICENCE', 80.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 IA'),(cm_id,'L1 CS'),(cm_id,'L1 G'),(cm_id,'L1 TC');

-- =============================================================
-- 4. MANSOUR DIALLO — Anglais / Management (5 modules → 3 + avenant 2)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Diallo', 'Mansour', 'mansour.diallo@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Anglais / Management', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Business English 3', 'LICENCE', 100.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 IAGE A'),(cm_id,'L2 IAGE B'),(cm_id,'L2 TTL A'),(cm_id,'L2 TTL B'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Business English 4', 'LICENCE', 100.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 IAGE A'),(cm_id,'L2 IAGE B'),(cm_id,'L2 TTL A'),(cm_id,'L2 TTL B'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Business English 1', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 C'),(cm_id,'L1 E'),(cm_id,'L1 D');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Business English 2', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 C'),(cm_id,'L1 E'),(cm_id,'L1 D');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Management des Processus', 'LICENCE', 160.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B'),(cm_id,'L3 TTL A'),(cm_id,'L3 TTL B'),
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 MAE'),(cm_id,'L3 ETSE');

-- =============================================================
-- 5. GORA NDIAYE SAMB — Comptabilité (8 modules → 3 + avenant 5)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Ndiaye Samb', 'Gora', 'gora.samb@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Comptabilité', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Comptabilité de Gestion', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Gestion Budgétaire', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Travaux de Fin d''Exercice', 'LICENCE', 30.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 IAGE B');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Comptabilité Analytique', 'LICENCE', 120.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 IAGE B'),(cm_id,'L2 MAE'),(cm_id,'L2 MOSIEF'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Lecture et Analyse des États Financiers', 'LICENCE', 12.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 IAGE B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Facturation et Devis', 'LICENCE', 12.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 CPD');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Gestion comptable', 'LICENCE', 20.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 CPD');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Information Financière II', 'LICENCE', 63.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 IA'),(cm_id,'L2 CS'),(cm_id,'L2 TC');

-- =============================================================
-- 6. MALICK DIOP — Comptabilité (4 modules → 3 + avenant 1)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Diop', 'Malick', 'malick.diop@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Comptabilité', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Comptabilité de Gestion', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 TTL A'),(cm_id,'L3 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Gestion Budgétaire', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 TTL A'),(cm_id,'L3 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Comptabilité Générale 1', 'LICENCE', 32.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 B'),(cm_id,'L1 IA'),(cm_id,'L1 CS'),(cm_id,'L1 H'),(cm_id,'L1 TC');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Comptabilité Générale 2', 'LICENCE', 32.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 B'),(cm_id,'L1 IA'),(cm_id,'L1 CS'),(cm_id,'L1 H'),(cm_id,'L1 TC');

-- =============================================================
-- 7. COUNTA DIOP — Comptabilité (5 modules → 3 + avenant 2)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Diop', 'Counta', 'counta.diop@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Comptabilité', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Travaux de Fin d''Exercice', 'LICENCE', 30.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 IAGE A');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Comptabilité Analytique', 'LICENCE', 30.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 IAGE A');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Lecture et Analyse des États Financiers', 'LICENCE', 12.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 IAGE A');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Comptabilité Générale 1', 'LICENCE', 32.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 D');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Comptabilité Générale 2', 'LICENCE', 32.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 D');

-- =============================================================
-- 8. ABDOUL KHOUDOSS SY — Comptabilité (2 modules → contrat principal uniquement)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Sy', 'Abdoul Khoudoss', 'abdoulkhoudoss.sy@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Comptabilité', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Travaux de Fin d''Exercice', 'LICENCE', 30.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 TTL A');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Comptabilité Analytique', 'LICENCE', 30.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 TTL A');

-- =============================================================
-- 9. ELHADJI BABACAR GUEYE — Transport / Logistique (11 modules → 3 + avenant 8)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Gueye', 'Elhadji Babacar', 'elhadji.gueye@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Transport / Logistique', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Organisation et Gestion des Transports Maritimes 1', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 TTL A'),(cm_id,'L3 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Management des Opérations de Douane et de Transit 3', 'LICENCE', 32.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 TTL A'),(cm_id,'L3 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Économie du Transport', 'LICENCE', 48.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 TTL A'),(cm_id,'L3 TTL B');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Organisation et Gestion des Transports Terrestres', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 TTL A'),(cm_id,'L2 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Management des Opérations de Douane et de Transit 1', 'LICENCE', 32.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 TTL A'),(cm_id,'L2 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Gestion des Approvisionnements', 'LICENCE', 48.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 TTL A'),(cm_id,'L3 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Organisation et Gestion des Transports Maritimes 2', 'LICENCE', 30.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 TTL A'),(cm_id,'L3 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Management des Opérations de Douane et de Transit 4', 'LICENCE', 16.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 TTL A'),(cm_id,'L3 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Organisation et Gestion des Transports Aériens', 'LICENCE', 48.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 TTL A'),(cm_id,'L2 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Management des Opérations de Douane et de Transit 2', 'LICENCE', 48.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 TTL A'),(cm_id,'L2 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Logistique de Production', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 TTL A'),(cm_id,'L3 TTL B');

-- =============================================================
-- 10. IBRAHIMA NDIAYE — Logistique (2 modules → contrat principal uniquement)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Ndiaye', 'Ibrahima', 'ibrahima.ndiaye@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Logistique', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Chaine Logistique Globale', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 TTL A'),(cm_id,'L2 TTL B');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Management de la Chaine Logistique', 'LICENCE', 48.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 TTL A'),(cm_id,'L2 TTL B');

-- =============================================================
-- 11. ROBERT MADOUNE SEYE — Télécoms / Réseaux (5 modules → 3 + avenant 2)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Seye', 'Robert Madoune', 'robert.seye@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Télécoms / Réseaux', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Systèmes Embarqués & IOT 1', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 ETSE');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Architecture des Réseaux de Télécoms', 'LICENCE', 20.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 ETSE');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Réseaux Sans Fil: WiFi et WiMax', 'LICENCE', 20.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 ETSE');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Réseaux Mobiles', 'LICENCE', 20.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 ETSE');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Systèmes Embarqués & IOT 2', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 ETSE');

-- =============================================================
-- 12. LAMINE SANE — Télécoms (2 modules → contrat principal uniquement)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Sane', 'Lamine', 'lamine.sane@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Télécoms', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Faisceaux Hertiens et VSAT', 'LICENCE', 24.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 ETSE');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Supports et Systèmes de Transmission', 'LICENCE', 30.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 ETSE');

-- =============================================================
-- 13. SERIGNE MODOU KARA SAMB — Bases de données (2 modules → contrat principal uniquement)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Samb', 'Serigne Modou Kara', 'serigne.samb@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Bases de données', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Systèmes de Gestion de Bases de Données', 'LICENCE', 240.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 GLRS A'),(cm_id,'L2 GLRS B'),(cm_id,'L2 ETSE'),(cm_id,'L2 IAGE A'),
        (cm_id,'L2 IAGE B'),(cm_id,'L2 MAE'),(cm_id,'L2 CDSD'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Administration de BDD sous SQL Server', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B');

-- =============================================================
-- 14. BIRANE BAILA WANE — Informatique / Génie Logiciel (9 modules → 3 + avenant 6)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Wane', 'Birane Baila', 'birane.wane@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Informatique / Génie Logiciel', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Programmation Objet 6: JAVA', 'LICENCE', 96.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 CDSD'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Programmation Web 2: PHP POO / Symfony', 'LICENCE', 96.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 MAE'),(cm_id,'L3 CDSD'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Technologies .NET (C#)', 'LICENCE', 48.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Algorithmique & Structures de Données 2', 'LICENCE', 210.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 GLRS A'),(cm_id,'L2 GLRS B'),(cm_id,'L2 ETSE'),(cm_id,'L2 MAE'),
        (cm_id,'L2 MOSIEF'),(cm_id,'L2 CDSD'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Programmation Web 1: PHP', 'LICENCE', 120.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 GLRS A'),(cm_id,'L2 GLRS B'),(cm_id,'L2 MAE'),(cm_id,'L2 CDSD'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Analyse et Conception 1 (UML)', 'LICENCE', 140.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 GLRS A'),(cm_id,'L2 GLRS B'),(cm_id,'L2 MAE'),(cm_id,'L2 MOSIEF'),
        (cm_id,'L2 CDSD'),(cm_id,'L2 ETSE'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Algorithmique et Langages de Programmation', 'LICENCE', 120.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 C'),(cm_id,'L1 D'),(cm_id,'L1 CDSD'),(cm_id,'L1 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Développement d''Applications Mobile Flutter', 'LICENCE', 96.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 CDSD'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Applications Angular', 'LICENCE', 120.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 CDSD'),(cm_id,'L3 TC');

-- =============================================================
-- 15. ALY TALL NIANG — Dev Web / Mobile / Python (10 modules → 3 + avenant 7)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Niang', 'Aly Tall', 'aly.niang@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Dev Web / Mobile / Python', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Application Mobile', 'LICENCE', 24.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 CPD');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'TypeScript - React.js 2', 'LICENCE', 20.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 CDSD');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Framework Laravel', 'LICENCE', 16.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 CDSD');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Programmation Objet 2: Python', 'LICENCE', 270.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 GLRS A'),(cm_id,'L2 GLRS B'),(cm_id,'L2 ETSE'),(cm_id,'L2 IAGE A'),
        (cm_id,'L2 IAGE B'),(cm_id,'L2 MAE'),(cm_id,'L2 MOSIEF'),(cm_id,'L2 CDSD'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Programmation C', 'LICENCE', 120.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 GLRS A'),(cm_id,'L2 GLRS B'),(cm_id,'L2 ETSE'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Javascript / jQuery 2', 'LICENCE', 72.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 CPD'),(cm_id,'L2 CDSD'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Programmation Orientée Objet 1: Python', 'LICENCE', 192.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 A'),(cm_id,'L1 B'),(cm_id,'L1 C'),(cm_id,'L1 E'),
        (cm_id,'L1 G'),(cm_id,'L1 F'),(cm_id,'L1 H'),(cm_id,'L1 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Framework Python: Flask', 'LICENCE', 96.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 CDSD'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Node.js / Express.js', 'LICENCE', 20.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 CDSD');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Vue.js / Angular.js', 'LICENCE', 20.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 CDSD');

-- =============================================================
-- 16. MASSAMBA LO — Réseaux / Administration (7 modules → 3 + avenant 4)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Lo', 'Massamba', 'massamba.lo@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Réseaux / Administration Systèmes', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Administration de Réseaux Sous Linux Server', 'LICENCE', 120.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 ETSE'),(cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Interconnexion de Réseaux: CISCO', 'LICENCE', 96.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 ETSE'),(cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Administration Système Windows', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 GLRS A'),(cm_id,'L2 GLRS B');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Théories des Systèmes d''Exploitation', 'LICENCE', 80.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 C'),(cm_id,'L1 E'),(cm_id,'L1 G'),(cm_id,'L1 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Pratique des Systèmes d''Exploitation', 'LICENCE', 80.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 C'),(cm_id,'L1 E'),(cm_id,'L1 G'),(cm_id,'L1 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Administration Systèmes Linux', 'LICENCE', 150.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 GLRS A'),(cm_id,'L2 GLRS B'),(cm_id,'L2 ETSE'),(cm_id,'L2 MAE'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Administration de Réseaux sous Windows Server', 'LICENCE', 180.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 ETSE'),(cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B'),(cm_id,'L3 TC');

-- =============================================================
-- 17. OUSMANE DIAGNE — Réseaux CISCO (2 modules → contrat principal uniquement)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Diagne', 'Ousmane', 'ousmane.diagne@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Réseaux CISCO', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'CISCO IT Essentials 1', 'LICENCE', 240.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 A'),(cm_id,'L1 B'),(cm_id,'L1 C'),(cm_id,'L1 E'),
        (cm_id,'L1 F'),(cm_id,'L1 G'),(cm_id,'L1 H'),(cm_id,'L1 IA'),(cm_id,'L1 CS'),(cm_id,'L1 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Fondamentaux des Réseaux: CCNA 1', 'LICENCE', 240.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 A'),(cm_id,'L1 B'),(cm_id,'L1 C'),(cm_id,'L1 E'),
        (cm_id,'L1 F'),(cm_id,'L1 G'),(cm_id,'L1 H'),(cm_id,'L1 IA'),(cm_id,'L1 CS'),(cm_id,'L1 TC');

-- =============================================================
-- 18. ALBERT DIOMPY — Mathématiques / Statistiques (6 modules → 3 + avenant 3)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Diompy', 'Albert', 'albert.diompy@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Mathématiques / Statistiques', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Mathématiques et Décisions Financières 2', 'LICENCE', 100.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 MAE'),(cm_id,'L3 MOSIEF'),(cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Risque Crédit', 'LICENCE', 60.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 MAE'),(cm_id,'L3 MOSIEF'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Statistique inférentielle 1', 'LICENCE', 120.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 MAE'),(cm_id,'L2 MOSIEF'),(cm_id,'L2 TC');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Analyse des données 1', 'LICENCE', 90.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 MAE'),(cm_id,'L2 MOSIEF'),(cm_id,'L2 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Statistiques Descriptives', 'LICENCE', 50.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 D');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Mathématiques 1', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L1 D');

-- =============================================================
-- 19. ABDOUL MAGIB DIAGNE — RH / Droit du Travail (3 modules → contrat principal uniquement)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Diagne', 'Abdoul Magib', 'abdoulmagib.diagne@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'RH / Droit du Travail', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Acquisition des Ressources Humaines', 'LICENCE', 100.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B'),(cm_id,'L3 TTL A'),(cm_id,'L3 TTL B'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Fondements du Droit du Travail', 'LICENCE', 160.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 GLRS A'),(cm_id,'L3 GLRS B'),(cm_id,'L3 ETSE'),(cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B'),
        (cm_id,'L3 TTL A'),(cm_id,'L3 TTL B'),(cm_id,'L3 CPD'),(cm_id,'L3 CDSD'),(cm_id,'L3 TC');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'Administration des Ressources Humaines', 'LICENCE', 100.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 IAGE A'),(cm_id,'L3 IAGE B'),(cm_id,'L3 TTL A'),(cm_id,'L3 TTL B'),(cm_id,'L3 TC');

-- =============================================================
-- 20. MELBA ORLIE NZANG — UX/UI Design (5 modules → 3 + avenant 2)
-- =============================================================
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Nzang', 'Melba Orlie', 'melba.nzang@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'UX/UI Design', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', false, 'ACTIF', NOW())
    RETURNING id INTO c_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'UX Design: Ergonomie mobile', 'LICENCE', 28.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 CPD'),(cm_id,'L3 CDSD');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'UI Design', 'LICENCE', 28.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L3 CPD'),(cm_id,'L3 CDSD');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c_id, 'UX Design: Ergonomie responsive & Prototypage', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 CPD'),(cm_id,'L2 CDSD');

    -- Avenant
    INSERT INTO contrats (vacataire_id, annee_academique, date_debut, date_fin, est_avenant, statut, contrat_parent_id, date_creation)
    VALUES (v_id, '2025-2026', '2025-09-01', '2026-09-30', true, 'ACTIF', c_id, NOW())
    RETURNING id INTO c2_id;

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'UI Design: Design d''Interface', 'LICENCE', 28.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 CPD'),(cm_id,'L2 CDSD');

    INSERT INTO contrat_modules (contrat_id, nom_module, niveau, volume_horaire_previsionnel, date_debut_prevue)
    VALUES (c2_id, 'Book de Compétence/Portfolio Digital', 'LICENCE', 40.0, '2025-09-01') RETURNING id INTO cm_id;
    INSERT INTO contrat_module_classes (contrat_module_id, classe) VALUES
        (cm_id,'L2 CPD'),(cm_id,'L2 CDSD');

-- =============================================================
-- CAS FORFAIT — Utilisateurs sans contrat horaire
-- Contrats à créer manuellement dans l'application
-- =============================================================

-- 21. FULBERT MISSEKPE — Informatique (Forfait 300 000 FCFA)
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Missekpe', 'Fulbert', 'fulbert.missekpe@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Informatique (Forfait 300 000 FCFA)', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

-- 22. SIDY MOHAMED SAIZOUNOU — Informatique (Forfait 300 000 FCFA)
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Saizounou', 'Sidy Mohamed', 'sidy.saizounou@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Informatique (Forfait 300 000 FCFA)', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

-- 23. SOKHNA OUMY LAYE DIOP — Comptabilité (Forfait)
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Diop', 'Sokhna Oumy Laye', 'sokhna.diop@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Comptabilité (Forfait)', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

-- 24. CLEMENT TEOU — Design / Multimédia (Forfait 150 000 FCFA)
    INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, premier_connexion, date_creation)
    VALUES ('Teou', 'Clement', 'clement.teou@ism.edu.sn', MDK, 'VACATAIRE', true, true, NOW())
    ON CONFLICT (email) DO UPDATE SET mot_de_passe = EXCLUDED.mot_de_passe, actif = true
    RETURNING id INTO u_id;

    INSERT INTO vacataires (utilisateur_id, specialite, type_vacataire, statut)
    VALUES (u_id, 'Design / Multimédia (Forfait 150 000 FCFA)', 'STANDARD', 'ACTIF')
    ON CONFLICT (utilisateur_id) DO UPDATE SET specialite = EXCLUDED.specialite
    RETURNING id INTO v_id;

END $$;
