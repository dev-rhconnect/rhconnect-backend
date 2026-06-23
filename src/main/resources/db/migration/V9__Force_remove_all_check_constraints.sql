-- V8: Supprimer toutes les contraintes CHECK sur les colonnes niveau

-- Supprimer contraintes CHECK sur vacataire_niveaux
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    FOR constraint_name IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON con.conrelid = rel.oid
        WHERE rel.relname = 'vacataire_niveaux' AND con.contype = 'c'
    LOOP
        EXECUTE 'ALTER TABLE vacataire_niveaux DROP CONSTRAINT IF EXISTS "' || constraint_name || '"';
    END LOOP;
END $$;

-- Supprimer contraintes CHECK sur contrat_modules
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    FOR constraint_name IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON con.conrelid = rel.oid
        WHERE rel.relname = 'contrat_modules' AND con.contype = 'c'
    LOOP
        EXECUTE 'ALTER TABLE contrat_modules DROP CONSTRAINT IF EXISTS "' || constraint_name || '"';
    END LOOP;
END $$;
