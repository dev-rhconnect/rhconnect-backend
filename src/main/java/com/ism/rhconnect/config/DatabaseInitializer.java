package com.ism.rhconnect.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
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

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_vacataire_modules_vacataire_id ON vacataire_modules(vacataire_id)");

            // Rendre nullable les colonnes héritées de l'ancien schéma
            stmt.execute("ALTER TABLE contrats ALTER COLUMN classe DROP NOT NULL");
            stmt.execute("ALTER TABLE contrats ALTER COLUMN module DROP NOT NULL");
            stmt.execute("ALTER TABLE contrats ALTER COLUMN taux_horaire DROP NOT NULL");
            stmt.execute("ALTER TABLE contrats ALTER COLUMN volume_horaire_previsionnel DROP NOT NULL");
            stmt.execute("ALTER TABLE contrats ALTER COLUMN date_debut DROP NOT NULL");
            stmt.execute("ALTER TABLE contrats ALTER COLUMN date_fin DROP NOT NULL");

        } catch (Exception e) {
            System.err.println("Database init warning: " + e.getMessage());
        }

        // Exécuter vacataires_2025_2026.sql seulement si les contrats n'existent pas encore
        // (spring.sql.init ne gère pas le dollar-quoting PostgreSQL)
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM contrats WHERE annee_academique = '2025-2026'"
            );
            rs.next();
            int existingContrats = rs.getInt(1);
            rs.close();

            if (existingContrats == 0) {
                try (InputStream is = new ClassPathResource("vacataires_2025_2026.sql").getInputStream()) {
                    String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    stmt.execute(sql);
                }
            }

        } catch (Exception e) {
            System.err.println("Vacataires init warning: " + e.getMessage());
        }
    }
}
