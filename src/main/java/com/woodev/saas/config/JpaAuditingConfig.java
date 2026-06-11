package com.woodev.saas.config;

import com.woodev.saas.entities.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class JpaAuditingConfig {

    //Créer un bean de type AuditorAware de type string.
    //Le type doit être le type de notre clé primaire (String)
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    //AuditorAware<String>
    //               ↑
    //     Type de la clé primaire !
    //     Notre id = String (UUID)
    // Si id était Long :
    // AuditorAware<Long>
    public static  class AuditorAwareImpl implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            //Cette ligne récupère l'objet Authentication à partir du contexte de sécurité de Spring.
            //Cet objet contient des informations sur l'utilisateur actuellement authentifié.
            //Cela te permet de connaître l'utilisateur actuellement connecté.
            //SecurityContextHolder.getContext() : Mis à jour par JwtAuthenticationFilter
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //Verifie que l'utilisateur est authentifié et qu'il n'est pas un utilisateur anonyme.
            //Si l'une de ces conditions n'est pas remplie, la méthode retourne Optional.empty(),
            //indiquant qu'il n'y a pas d'auditeur disponible.
            if (authentication == null ||
                    !authentication.isAuthenticated() ||
                            authentication.getPrincipal().equals("anonymousUser"))
            {
                return Optional.empty();
            }

            if(authentication.getPrincipal() != null) {
                return Optional.of(authentication.getPrincipal().toString());
            }

            return Optional.empty();
        }
    }

}
