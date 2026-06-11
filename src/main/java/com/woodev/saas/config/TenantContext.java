package com.woodev.saas.config;

//TenantContext va stocker les informations du tenantId.
/**
 * TenantContext - Stocke l'identifiant du tenant courant dans un ThreadLocal.
 * Chaque requete HTTP est traitée par un thread distinct (dédié).
 * Le ThreadLocal garantit que le tenantId est isolé par thread
 * même en cas de requête simultanée de tenants différents.
 *
 * Flux:
 *      1- TenantFilter extrait le tenant_id de la requete HTTP
 *      2- TenantFilter appelle TenantContext.setCurrentTenant(tenant_id)
 *      3- Le code métier (service, repositories) accède au tenant via TenantContext.getCurrentTenant()
 *      4- TenantFilter appelle TenantContext.clear() après la réponse (nettoyage du contexte)
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_SCHEMA = new ThreadLocal<>();


    /**
     * Recupere l'identifiant du tenant pour le thread courant.
     * @return  tenantId
     */
    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static String getCurrentSchema() {
        return CURRENT_SCHEMA.get();
    }

    /**
     * Definit l'identifiant du tenant pour le thread courant.
     * @param tenant
     */
    public static void setCurrentTenant(final String tenant) {
        CURRENT_TENANT.set(tenant);
    }
    public static void setCurrentSchema(String schemaName) {
        CURRENT_SCHEMA.set(schemaName);
    }


    //Nettoyer le contexte
    /**
     * Nettoie le tenant du thread courant.
     * IMPORTANT: doit être appelé dans un bloc finnaly
     * pour eviter les fuites de memoire (memory leak).
     * et les fuites de données entre requetes HTTP.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_SCHEMA.remove();
    }


}