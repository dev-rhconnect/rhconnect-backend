ALTER TABLE vacataires
    ADD COLUMN IF NOT EXISTS date_naissance DATE,
    ADD COLUMN IF NOT EXISTS lieu_naissance VARCHAR(255),
    ADD COLUMN IF NOT EXISTS nationalite    VARCHAR(100);
