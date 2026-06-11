package com.woodev.saas.entities;

public enum TenantStatus {
    // → Demande d'inscription envoyée
    // → En attente d'approbation
    PENDING,

    // → Approuvé par PLATFORM_ADMIN
    // → Schéma créé
    // → Peut utiliser l'app
    ACTIVE,

    // → Temporairement bloqué
    // → Schéma EXISTE toujours
    // → Accès refusé au login
    SUSPENDED,

    // → Désactivé définitivement
    // → Compte fermé
    INACTIVE
}
