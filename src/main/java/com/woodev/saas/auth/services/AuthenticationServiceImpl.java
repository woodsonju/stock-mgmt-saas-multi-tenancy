package com.woodev.saas.auth.services;

import com.woodev.saas.auth.LoginRequest;
import com.woodev.saas.auth.LoginResponse;
import com.woodev.saas.entities.User;
import com.woodev.saas.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService{

    //Utiliser pour l'authentification des utilisateurs
    private final AuthenticationManager authenticationManager;

    private final JwtTokenService jwtTokenService;

    @Override
    public LoginResponse login(final LoginRequest request) {

        // ÉTAPE 1 : Authentifier
        //Cette méthode utilise un AuthenticationManager pour authentifier l'utilisateur.
        //Retourne un objet de type Authentication, qui est l'utilisateur qu'on va charger depuis notre base de données
        //Quand la méthode authenticate est appelée, voici ce qui se passe, Le processus étape par étape :
        //1- L'AuthenticationManager reçoit le username et le password que l'utilisateur a saisis dans le formulaire de login.
        //2- Il transmet ces informations à un composant interne de Spring appelé le DaoAuthenticationProvider.
        //3- Ce DaoAuthenticationProvider a pour rôle de comparer le mot de passe saisi avec le mot de passe stocké en base de données.
        //4- Pour récupérer l'utilisateur en base de données, le DaoAuthenticationProvider cherche dans ton projet s'il existe
        //un Bean Spring qui implémente l'interface UserDetailsService.
        //5- C'est là qu'il trouve ton UserServiceImpl ! Il appelle automatiquement ta méthode loadUserByUsername(username).
        //6- Une fois qu'il a récupéré ton entité User (qui implémente UserDetails), le provider compare le mot de passe fourni
        //avec celui de la base (en utilisant le PasswordEncoder). Si ça correspond, l'utilisateur est authentifié.
        //Ton service de login (AuthenticationServiceImpl) n'a pas besoin de savoir comment les utilisateurs
        //sont stockés (dans une DB, dans un fichier, dans un annuaire LDAP). Il dit juste à Spring : "Authentifie-moi ce gars-là".
        final Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        //ÉTAPE 2 :Récupérer le User de l'objet Authentication'
        final User user = (User) authentication.getPrincipal();
        //ÉTAPE 3 : Générer le JWT
        final String token = this.jwtTokenService.generateAccessToken(user.getTenantId(), user.getId(), user.getRole().name());
        final String tokenType = "Bearer";

        //ÉTAPE 4 :Retourner la réponse
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType(tokenType)
                .build();
    }


}
