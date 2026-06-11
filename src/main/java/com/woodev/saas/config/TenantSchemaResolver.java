package com.woodev.saas.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

//CacheConfig crée le tiroir
//CacheConfig = "Je configure le système de cache"
//TenantSchemaResolver remplit/lit le tiroir
//TenantSchemaResolver = "Je trouve le schéma d'un tenant
//                        et je mémorise le résultat"
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSchemaResolver {

    private static final String PUBLIC_SCHEMA = "public";

    private final JdbcTemplate jdbcTemplate;

    //Lorsque je fais appel à la méthode resolveTenantSchema et que je passe le tenantId
    //On va vérifier le cache et si nous avons une entrée dans le cache avec le tenantId qu'on a passé en paramètre
    //On va plus faire appel à la base de données, on va retourner automatiquement la valeur qui est déjà stockée dans le cache
    @Cacheable(value = "tenantSchemas",    // Nom du cache, c'est le tenantSchemas qu'on a fait dans CacheConfig
        key = "#tenantId"
    )
    public String resolveTenantSchema(String tenantId) {

        if (tenantId == null) {
            return PUBLIC_SCHEMA;
        }

        try {
            final String companyCode = this.jdbcTemplate.queryForObject(
                    "SELECT company_code FROM public.tenants WHERE id = ? AND deleted = false AND tenant_status = 'ACTIVE'",
                    String.class, tenantId);

            if(companyCode != null) {
                final String schemaName = "tenant_"  + companyCode.toLowerCase();
                log.debug("Resolved Tenant Schema: {} for tenant: {}", schemaName, tenantId);
                return schemaName;   //Mise en cache
            }

            log.warn("Tenant schema not found for tenant: {}, using public schema", tenantId);
            return PUBLIC_SCHEMA; // "public" par défaut

        }catch (Exception e) {
            log.error("Error resolving tenant schema for tenant: {}", tenantId, e);
            return PUBLIC_SCHEMA; //Si je ne trouve pas le schema, je pointe automatiquement vers le schéma public
        }

    }

}
