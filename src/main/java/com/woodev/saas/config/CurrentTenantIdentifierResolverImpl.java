package com.woodev.saas.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

//On doit donner la possibilité à Hibernate de terminer ou bien d'identifier le schéma".
//→ Hibernate a besoin de savoir :
//  "Dans QUEL schéma dois-je exécuter mes requêtes SQL ?"
//→ CurrentTenantIdentifierResolver
//  est le "messager" qui dit à Hibernate
//  quel schéma utiliser
//CurrentTenantIdentifierResolverImpl est le pont entre le ThreadLocal et Hibernate —
//à chaque requête SQL, Hibernate la consulte pour savoir dans quel schéma travailler.
//Elle lit simplement le TenantContext et retourne le nom du schéma.
@Component
@Slf4j
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    // Schéma par défaut au démarrage
    private static final String PUBLIC_SCHEMA = "public";

    // Dire à Hibernate quel schéma utiliser
    // Quand cette méthode est-elle appelée ?
    // → AVANT chaque requête SQL Hibernate
    // → Hibernate demande : "Quel schéma ?"
    // → Cette méthode répond : "tenant_woodev"
    @Override
    public String resolveCurrentTenantIdentifier() {
        final String schema = TenantContext.getCurrentSchema();
        log.trace("Resolving current tenant schema: identifier: {}", schema);

        //Si pas de schéma défini -> retourner "public"
        if(schema == null || schema.isBlank()) {
            return "public";
            // → Hibernate utilise "public" au démarrage ✅
            // → Users et Tenants sont dans public

        }
        return schema;  // Ex:  "tenant_woodev" ← dit à Hibernate
        //   dans quel schéma travailler
    }

    // Valider la session courante
    @Override
    public boolean validateExistingCurrentSessions() {
        return true;  // true = Hibernate vérifie que la session ouverte appartient bien au tenant courant
    }

    // Enregistrer cette classe dans Hibernate
    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }


}

