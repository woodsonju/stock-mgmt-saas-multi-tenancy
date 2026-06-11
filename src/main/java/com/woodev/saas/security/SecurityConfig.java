package com.woodev.saas.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    //Les routes accessibles sans auth
    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",                  // Login/Register
            "/v2/api-docs",                     // Swagger
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",

    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth ->
                        // public routes
                        auth.requestMatchers(PUBLIC_URLS) //Les URLs dans PUBLIC_URLS sont ACCESSIBLES sans authentification
                                .permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**")  //Autorisé tous les méthodes OPTIONS pour n'importe quels endpoints. Nécessaire pour CORS
                                .permitAll()
                                //All others routes require authentication
                                .anyRequest()
                                .authenticated()  // Exige l'authentification pour toutes les autres requêtes
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))  //STATELESS = Pas de session côté serveur
                .addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Exécuter le filtre jwtAuthenticationFilter avant le UsernamePasswordAuthenticationFilter

            return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        //Spécifier les origines autorisées
        //Cette ligne spécifie les origines (domaines) autorisées à faire des requêtes vers votre serveur.
        //Par exemple, seule l'origine http://localhost:4200 est autorisée, ce qui est typique pour une application
        //Angular en développement.
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); //Configure property in production

        //Spécifier les méthodes autorisées
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        //Spécifier les en-têtes autorisés
        configuration.setAllowedHeaders(Arrays.asList(
                HttpHeaders.ORIGIN,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION
        ));

        //Autoriser les informations d'identification (cookies, authentifications)
        //Cette méthode permet d'envoyer des cookies ou d'autres informations d'identification avec les requêtes CORS.
        //C'est nécessaire si votre application a besoin de s'authentifier via des cookies ou des jetons envoyés dans les en-têtes.
        configuration.setAllowCredentials(true);

        // Le navigateur met en CACHE les résultats OPTIONS
        // → Si le client appelle 10 fois /api/products
        // → Seulement 1 OPTIONS envoyé (au lieu de 10)
        // → Performance ++
        // Valeurs courantes :
        // → 1h (3600) pour dev
        // → 24h (86400) pour prod
        configuration.setMaxAge(3600L); //1 heure

        //Création d'une configuration CORS
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        //Enregistrement de la configuration CORS
        //Enregistre la configuration CORS pour toutes les URL (/**) de l'application.
        //Cela signifie que toutes les requêtes vers n'importe quelle route de l'application devront respecter cette configuration CORS.
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
