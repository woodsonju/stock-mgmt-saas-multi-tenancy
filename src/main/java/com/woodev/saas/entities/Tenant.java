package com.woodev.saas.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "tenants")
public class Tenant extends AbstractEntity{

    // "Société Alpha, Beta, etc." (Bouali Ali)
    // Le nom commercial de l'entreprise
    // Ex: "Société Gamma SARL"
    // Pas unique → 2 sociétés peuvent avoir des noms similaires
    @Column(name="company_name", nullable = false)
    private String companyName;  //Société Alpha, Beta, etc.

    // Code UNIQUE qui identifie l'entreprise
    @Column(name="company_code", nullable = false, unique = true)
    private String companyCode;

    //Email principal de l'administrateur
    //C'est avec cet email qu'on va initialiser un utilisateur par défaut
    //pour qu'il puisse utiliser notre application.
    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name="tenant_status", nullable = false)
    private TenantStatus tenantStatus = TenantStatus.PENDING;

    //Initial admin crédential : informations de base de l'administrateur
    //On va utiliser ces informations pour créer un administrateur pour cette société une fois l'administrateur
    //de la plateforme approuve la demande de ce tenant là.
    @Column(name="admin_full_name", nullable = false)
    private String adminFullName;
    @Column(name="admin_email", nullable = false, unique = true)
    private String adminEmail;
    @Column(name="admin_username", nullable = false, unique = true)
    private String adminUsername;
    @Column(name="admin_password", nullable = false)
    private String adminPassword;


}
