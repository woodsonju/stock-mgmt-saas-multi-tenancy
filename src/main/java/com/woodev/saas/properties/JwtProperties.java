package com.woodev.saas.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// @ConfigurationProperties(prefix = "app.jwt") :
// "Va chercher dans application.yml
// toutes les propriétés qui commencent
// par app.jwt et mappe-les ici"
// Mapping automatique :
// app.jwt.private-key-path → privateKeyPath
// app.jwt.public-key-path  → publicKeyPath
// app.jwt.access-token-expiration → accessTokenExpiration
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String privateKeyPath;
    private String publicKeyPath;
    private long accessTokenExpiration;
}
