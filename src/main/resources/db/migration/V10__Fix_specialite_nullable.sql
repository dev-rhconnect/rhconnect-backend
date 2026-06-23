-- V10: S'assurer que specialite est nullable dans vacataires
ALTER TABLE vacataires ALTER COLUMN specialite DROP NOT NULL;
