package com.woodev.saas.services;

import com.woodev.saas.entities.Tenant;

public interface ProvisioningService {
    void provisionTenant(final Tenant tenant);
}
