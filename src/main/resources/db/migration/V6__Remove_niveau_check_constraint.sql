-- V6: Forcer la suppression de la contrainte CHECK sur niveau

ALTER TABLE IF EXISTS vacataire_niveaux
DROP CONSTRAINT IF EXISTS "vacataire_niveaux_niveau_check" CASCADE;

-- Alternative si le nom est différent
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    SELECT constraint_name INTO constraint_name
    FROM information_schema.check_constraints
    WHERE table_name = 'vacataire_niveaux' AND column_name = 'niveau'
    LIMIT 1;

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE vacataire_niveaux DROP CONSTRAINT %I', constraint_name);
    END IF;
END $$;
