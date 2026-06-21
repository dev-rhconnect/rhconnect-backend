-- V4: Rendre specialite nullable et ajouter table vacataire_modules

-- Rendre la colonne specialite nullable
ALTER TABLE vacataires ALTER COLUMN specialite DROP NOT NULL;

-- Créer la table vacataire_modules pour la relation @ElementCollection
CREATE TABLE IF NOT EXISTS vacataire_modules (
    vacataire_id BIGINT NOT NULL,
    module VARCHAR(255) NOT NULL,
    FOREIGN KEY (vacataire_id) REFERENCES vacataires(id) ON DELETE CASCADE
);

-- Créer un index pour les requêtes fréquentes
CREATE INDEX IF NOT EXISTS idx_vacataire_modules_vacataire_id ON vacataire_modules(vacataire_id);
