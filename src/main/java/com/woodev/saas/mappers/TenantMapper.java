package com.woodev.saas.mappers;

import com.woodev.saas.entities.Tenant;
import com.woodev.saas.requests.RegisterTenantRequest;
import com.woodev.saas.responses.TenantResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TenantMapper {

    public Tenant toEntity(final RegisterTenantRequest request){
        return Tenant.builder()
                .companyName(request.getCompanyName())
                .companyCode(request.getCompanyCode())
              //  .createdAt(LocalDateTime.now())  //@CreatedDate auto
                .email(request.getEmail())
                .adminFullName(request.getAdminFullName())
                .adminEmail(request.getAdminEmail())
                .adminUsername(request.getAdminUsername())
                //.adminPassword(request.getAdminPassword())  //On doit crypter le mot de passe, qui se fera dans le service
                .build();
    }


    public TenantResponse toResponse(final Tenant tenant) {
        return TenantResponse.builder()
                .tenantId(tenant.getId())
                .companyName(tenant.getCompanyName())
                .companyCode(tenant.getCompanyCode())
                .createAt(tenant.getCreatedAt())
                .email(tenant.getEmail())
                .adminFullName(tenant.getAdminFullName())
                .adminEmail(tenant.getAdminEmail())
                .adminUsername(tenant.getAdminUsername())
                .status(tenant.getTenantStatus())
                .build();
    }
}
