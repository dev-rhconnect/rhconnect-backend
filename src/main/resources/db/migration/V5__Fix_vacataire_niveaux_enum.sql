-- V5: Supprimer la contrainte CHECK restrictive sur vacataire_niveaux

-- Supprimer la contrainte CHECK existante si elle existe
DO $$
BEGIN
    ALTER TABLE vacataire_niveaux
    DROP CONSTRAINT vacataire_niveaux_niveau_check;
EXCEPTION WHEN undefined_object THEN
    NULL;
END $$;
