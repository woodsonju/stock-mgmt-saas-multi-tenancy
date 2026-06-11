package com.woodev.saas.services;

import com.woodev.saas.common.PageResponse;
import com.woodev.saas.requests.RegisterTenantRequest;
import com.woodev.saas.responses.TenantResponse;

public interface TenantService {

    void registerTenant(final RegisterTenantRequest registerTenantRequest);

    void approveTenant(final String tenantId); //L'administrateur de la plateforme, qui peut approuver le tenant.


    void  activateTenant(final String tenantId);

    void deactivateTenant(final String tenantId);

    void suspendTenant(final String tenantId);

    PageResponse<TenantResponse> findAll(final int page, final int size);
}
