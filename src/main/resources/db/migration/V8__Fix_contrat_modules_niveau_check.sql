-- V8: Supprimer la contrainte CHECK sur niveau dans contrat_modules
-- (elle n'acceptait que LICENCE et MASTER, on ajoute L1/L2/L3/MASTER_1/MASTER_2/DUT)

ALTER TABLE IF EXISTS contrat_modules
DROP CONSTRAINT IF EXISTS contrat_modules_niveau_check CASCADE;

-- Suppression générique si le nom exact diffère
DO $$
DECLARE
    cname TEXT;
BEGIN
    FOR cname IN
        SELECT cc.constraint_name
        FROM information_schema.constraint_column_usage ccu
        JOIN information_schema.check_constraints cc
          ON cc.constraint_name = ccu.constraint_name
        WHERE ccu.table_name = 'contrat_modules'
          AND ccu.column_name = 'niveau'
    LOOP
        EXECUTE format('ALTER TABLE contrat_modules DROP CONSTRAINT IF EXISTS %I', cname);
    END LOOP;
END $$;
