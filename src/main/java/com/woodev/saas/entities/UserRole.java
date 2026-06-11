package com.woodev.saas.entities;

public enum UserRole {

    //On ajoute ROLE_ car avec Spring security les roles commencent par ROLE_
    //Pourquoi le préfixe ROLE_ ?
    //Spring Security CONVENTION :
    //  hasRole("USER")    → cherche "ROLE_USER" en DB
    //  hasRole("ADMIN")   → cherche "ROLE_ADMIN" en DB
    //  hasAuthority("ROLE_USER") → cherche "ROLE_USER"
    ROLE_PLATFORM_ADMIN,  //Approuve les Tenants

    //"Le CompanyAdmin c'est le propriétaire
    // de la société et l'Administrateur
    // c'est le directeur"

    //COMPANY_ADMIN (Propriétaire de la société):
    //Droits COMPLETS sur les users
    //→ Gère toute la société
    //→ Doit voir la liste de TOUS les users
    //→ Pour les gérer (enable, disable, delete)
    ROLE_COMPANY_ADMIN, //Administrateur de la société (tenant)

    //ADMINISTRATOR(Directeur):
    //  → Droits PARTIELS
    //  → Peut VOIR les users (getAllUsers) ✅
    //  → NE PEUT PAS créer/supprimer ❌
    //  → NE PEUT PAS enable/disable ❌
    //  → Consulte mais ne gère pas
    ROLE_ADMINISTRATOR,

    ROLE_USER,

    ROLE_SALES_OPERATOR
}
