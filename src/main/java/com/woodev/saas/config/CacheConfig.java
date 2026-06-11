package com.woodev.saas.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Configuration du système de cache
//CacheConfig crée le tiroir
//TenantSchemaResolver remplit/lit le tiroir
@Configuration
@EnableCaching              //Activer le cache
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("tenantSchemas");
    }

}
