package com.woodev.saas.services.impl;

import com.woodev.saas.entities.Tenant;
import com.woodev.saas.exceptions.TenantProvisioningException;
import com.woodev.saas.services.ProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProvisioningServiceImpl implements ProvisioningService {

    // Pour exécuter du SQL natif (CREATE/DROP SCHEMA)
    //Pourquoi JdbcTemplate et pas JPA ?
    //JPA (Hibernate) :
    //→ Prévu pour les entités (CRUD)
    //→ Pas prévu pour DDL natif
    //→ CREATE SCHEMA n'est pas mappé
    //JdbcTemplate :
    //→ Exécute du SQL brut
    //→ Parfait pour DDL (CREATE, DROP, ALTER)
    //Règle :
    //→ JPA    = CRUD sur les entités
    //→ JDBC   = SQL natif, DDL, procédures
    private final JdbcTemplate jdbcTemplate;

    // Connexion à PostgreSQL
    // Passé à Flyway pour les migrations
    private final DataSource dataSource;

    @Override
    public void provisionTenant(final Tenant tenant) {
        // Convention de nommage du schéma
        // "Société Gamma" → companyCode "gamma"
        // → schemaName = "tenant_gamma"
        final String schemaName = "tenant_" + tenant.getCompanyCode().toLowerCase();
        try {
            log.info("Provisioning tenant: {} (schema: {})", tenant.getCompanyName(), schemaName);
            //1- Create the Postgres schema
            createSchema(schemaName);
            log.info("Schema created successfully: {}", schemaName);

            //2- Run Flyway migrations for this schema
            runTenantMigration(schemaName);
            log.info("Tenant migrations completed successfully for schema: {}", schemaName);

            //3- Initialize the default data (optional)
            initializeDefaultData(schemaName, tenant);
        } catch (Exception e) {
            log.error("Failed to provision tenant: {}", tenant.getCompanyName(), e);

            //rollback: Drop the schema creation
            try {
                dropSchema(schemaName);
            } catch (Exception ex) {
               log.error("Failed to rollback schema creation for tenant: {}", tenant.getCompanyName(), ex);
            }
            throw new TenantProvisioningException("Failed to provision tenant: {}" + tenant.getCompanyName());
        }
    }

    private void dropSchema(String schemaName) {
        final String sql =  String.format("DROP SCHEMA IF EXISTS %s CASCADE", schemaName);
        this.jdbcTemplate.execute(sql);
    }

    private void createSchema(String schemaName) {
        final String sql = String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName);
        this.jdbcTemplate.execute(sql);
    }

    private void runTenantMigration(String schemaName) {
        log.info("Running tenant migrations for schema: {}", schemaName);
        final Flyway tenantFlyWay = Flyway.configure()
                .dataSource(this.dataSource)   //Connexion à PostgreSQL
                .schemas(schemaName)        // Schéma cible : "Maintenant toutes mes requêtes  vont dans ce schéma !
                .locations("classpath:db/migration/tenant") //common sera exécuté automatiquement et le tenant sera exécuté par programmation.
                .baselineOnMigrate(true)
                .table("flyway_schema_history")
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load();

        log.info("Tenant migrations started");
        tenantFlyWay.migrate();
        log.info("Tenant migrations completed");
    }

    private void initializeDefaultData(String schemaName, Tenant tenant) {
        log.info("Initializing default data for tenant: {}", tenant.getCompanyName());
        //here you can add default data initialization code
    }
}




