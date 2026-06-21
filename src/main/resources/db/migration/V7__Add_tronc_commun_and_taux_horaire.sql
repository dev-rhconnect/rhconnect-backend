-- V7: Ajouter colonnes estTroncCommun et tauxHoraire

-- Ajouter colonne estTroncCommun à contrat_modules
ALTER TABLE IF EXISTS contrat_modules
ADD COLUMN IF NOT EXISTS est_tronc_commun BOOLEAN DEFAULT FALSE;

-- Ajouter colonne tauxHoraire à contrats
ALTER TABLE IF EXISTS contrats
ADD COLUMN IF NOT EXISTS taux_horaire BIGINT DEFAULT 10000;
