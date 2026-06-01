package com.woodev.saas.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

//Filtre java qui va intercepter chaque requête entrante et  va essayer
//d'extraire le tenant ID
//On donne un ordre très élevé comme ça il sera exécuté dès la réception de la requête

/**
 * Tenant - Intercepte CHAQUE requête pour identifier le tenant.
 * Ce filtre est le point d'entrée du mecanisme multi-tenants.
 * Il s'exécute avant tous les contrôleurs et services.
 *
 * Stratégie d'identification du tenant (par ordre de priorité) :
 * 1- Header X-Tenant-id
 * 2- (Optionnel) Sous-domaine : alpha.stockapp.com -> "alpha"
 *
 * Si aucun tenant n'est identifié -> réponse 400 BAD REQUEST
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-id";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //Transformer les ServletRequest et ServletResponse en un objet HttServletRequest
        //ServletRequest (générique) :
        //  → Peut être HTTP, FTP, etc.
        //  → Méthodes basiques seulement
        //HttpServletRequest (spécifique HTTP) :
        //  → getHeader() ← on en a besoin !
        //  → getMethod()
        //  → getCookies()
        //  → getSession()
        //Sans cast → impossible d'appeler getHeader()
        //Avec cast → accès à toutes les méthodes HTTP
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response =  (HttpServletResponse) servletResponse;

        final String tenantId = resolveTenant(request);
        if(tenantId == null || tenantId.isBlank()) {
           response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
           response.setContentType("application/json");
           response.getWriter().write("""
                    {"error":"Tenant ID is missing in the request header, please add the header X-Tenant-ID"}
                   """);
           return;
        }

        try {
            //Stocker le tenant dans le ThreadLocal
            TenantContext.setCurrentTenant(tenantId);
            //Continuer la chaine de filter -> controller -> service
            filterChain.doFilter(servletRequest, servletResponse); //Obligatoire
        } finally {
            //CRITIQUE : Toujours nettoyer le ThreadLocal après la requête
            //Sans la clear(), le tenant pourrait fuiter vers la requête suivante
            //si le thread est réutilisé par le pool de threads du serveur
            TenantContext.clear();
        }

    }

    private String resolveTenant(final HttpServletRequest request) {
        // Lire le header X-Tenant-id
        final String tenantId = request.getHeader(TENANT_HEADER);
        if(tenantId != null && !tenantId.isBlank()) {
            return tenantId.trim().toLowerCase();
        }
        return null; // Tenant absent
    }
}
