package com.ism.rhconnect.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Supprimer TOUTES les contraintes CHECK sur niveau
            stmt.execute("ALTER TABLE IF EXISTS vacataire_niveaux DROP CONSTRAINT IF EXISTS \"vacataire_niveaux_niveau_check\"");
            stmt.execute("ALTER TABLE IF EXISTS contrat_modules DROP CONSTRAINT IF EXISTS \"contrat_modules_niveau_check\"");

            // Supprimer aussi par nom générique
            try {
                stmt.execute("DO $$BEGIN " +
                        "ALTER TABLE vacataire_niveaux DROP CONSTRAINT IF EXISTS vacataire_niveaux_niveau_check; " +
                        "ALTER TABLE contrat_modules DROP CONSTRAINT IF EXISTS contrat_modules_niveau_check; " +
                        "END$$;");
            } catch (Exception ignored) {}

            // Créer la table vacataire_modules si elle n'existe pas
            stmt.execute("CREATE TABLE IF NOT EXISTS vacataire_modules (" +
                    "vacataire_id BIGINT NOT NULL," +
                    "module VARCHAR(255) NOT NULL," +
                    "FOREIGN KEY (vacataire_id) REFERENCES vacataires(id) ON DELETE CASCADE" +
                    ")");

            // Créer l'index
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_vacataire_modules_vacataire_id ON vacataire_modules(vacataire_id)");

        } catch (Exception e) {
            // Log mais continue - les tables peuvent déjà exister
            System.err.println("Database init warning: " + e.getMessage());
        }
    }
}
