package com.woodev.saas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//@EnableJpaAuditing(auditorAwareRef = "auditorAware") :
//Cette annotation active l'audit JPA.
//Pour le auditorAwareRef, on doit utiliser exactement le nom de la méthode
//ou bien on peut donner un nom à notre bean.
//Donc si on ne passe pas un nom à notre bean, le nom de la méthode sera
//automatiquement le nom du bean. (voir classe JpaAuditingConfig)
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
