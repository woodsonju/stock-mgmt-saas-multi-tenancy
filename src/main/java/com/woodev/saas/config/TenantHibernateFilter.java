package com.woodev.saas.config;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * TenantHibernateFilter  - Active automatiquement le filtre Hibernate
 * avant chaque appel aux repositories Spring Data JPA.
 *
 * FONCTIONNEMENT :
 *      1. TenantFilter (HTTP) a déjà stocké le tenantID dans le TenantContext
 *      2. Cet aspect intercepte tout appel à un Repository
 *      3. Il active le filtre Hibernate "tenantFilter" avec le tenantId courant
 *      4. Hibernate ajoute automatique WHERE tenant_id = :tenantId
 *
 * POURQUOI UN ASPECT ?
 *      Sans cet aspect, il faudrait activer le filtre manuellement dans chaque méthode de
 *      service. L'aspect le fait automatiquement et de manière transversale.
 *
 * ALTERNATIVE :
 *      On pourrait utiliser un HandlerInterceptor ou un @EventListener.
 *      L'aspect est plus propre car il s'exécute au plus proche de la couche de données
 */
//@Aspect
//@Component
public class TenantHibernateFilter {

    @PersistenceContext
    private EntityManager entityManager;

    //@Before("execution(* com.woodev.saas.services.*Service.*(..))") :
    //Explication :
    // Avant l'exécution de n'importe quelle méthode dans com.woodev.saas.services
    //.*Service -> point n'importe quelle service. Cela va chercher n'importe quelle classe
    //qui contient le nom Service dans le package com.woodev.saas.services
    //Ici on va mettre on va mettre *.*(..) au lieu de *Service.*(..)
    /**
     * Intercepte tous les appels aux méthodes des services dans le package com.woodev.saas.services
     *
     * execution(              type : exécution de méthode
     *     *                   n'importe quel type de retour
     *                         (void, List, Optional, etc.)
     *     com.woodev.saas     package racine
     *     .repositories       sous-package
     *     .*                  n'importe quelle CLASSE
     *                         (CategoryServiceIml, ProductServiceImpl...)
     *     .*                  n'importe quelle MÉTHODE
     *                         (findAll, save, findById...)
     *     (..)                n'importe quels ARGUMENTS
     *                         (0, 1, ou plusieurs params)
     */
    @Before("execution(* com.woodev.saas.services.impl.*.*(..))")
    public void activateTenantFilter() {
        //Récupérer le tenant depuis ThreadLocal (TenantContext)
        final String tenantId = TenantContext.getCurrentTenant();
        //Vérifier que le tenant existe
        //Si tenant est différent de nul on va créer une session
        if(tenantId != null) {
            //Obtenir la session Hibernate
            //Convertit JPA EntityManager en Session Hibernate native
            //Session = connexion DB courante
           final Session session = this.entityManager.unwrap(Session.class); //Session crée par Hibernate
            //Activer le filtre et inject le paramètre tenantId
            //et ajoute le paramètre tenantId à notre requête
            //Donc Toutes les requêtes de cette session auront automatiquement la condition WHERE tenant_id = ?
            //tenantFilter est le bean crée par Spring du composant TenantFilter
           session
                   .enableFilter("tenantFilter")// ← ACTIVE le filtre
                   .setParameter("tenantId", tenantId); // ← injecte la valeur
        }
    }
}
