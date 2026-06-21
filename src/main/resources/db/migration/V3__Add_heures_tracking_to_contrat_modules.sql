-- Ajouter colonnes de suivi des heures à contrat_modules
ALTER TABLE contrat_modules ADD COLUMN IF NOT EXISTS heures_effectuees NUMERIC(10, 2) DEFAULT 0;
ALTER TABLE contrat_modules ADD COLUMN IF NOT EXISTS heures_restantes NUMERIC(10, 2) DEFAULT 0;
