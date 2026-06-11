package com.woodev.saas.services.impl;

import com.woodev.saas.common.PageResponse;
import com.woodev.saas.entities.Tenant;
import com.woodev.saas.entities.TenantStatus;
import com.woodev.saas.entities.User;
import com.woodev.saas.entities.UserRole;
import com.woodev.saas.exceptions.DuplicateResourceException;
import com.woodev.saas.exceptions.InvalidRequestException;
import com.woodev.saas.mappers.TenantMapper;
import com.woodev.saas.repositories.TenantRepository;
import com.woodev.saas.repositories.UserRepository;
import com.woodev.saas.requests.RegisterTenantRequest;
import com.woodev.saas.responses.TenantResponse;
import com.woodev.saas.services.ProvisioningService;
import com.woodev.saas.services.TenantService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ProvisioningService provisioningService;

    @Override
    @Transactional
    public void registerTenant(RegisterTenantRequest registerTenantRequest) {

        //check if tenant already exists by company code
        if(this.tenantRepository.existsByCompanyCode(registerTenantRequest.getCompanyCode())) {
            log.debug("Tenant already exists");
            throw new DuplicateResourceException("Tenant already exists");
        }

        //check if email already exists
        if(this.tenantRepository.existsByEmail(registerTenantRequest.getEmail())) {
            log.debug("Email already exists");
            throw new DuplicateResourceException("Email already exists");
        }

        //create Tenant Entity
        final Tenant tenant = this.tenantMapper.toEntity(registerTenantRequest);
        tenant.setAdminPassword(this.passwordEncoder.encode(registerTenantRequest.getAdminPassword())); // ← depuis la REQUEST !
        tenant.setTenantStatus(TenantStatus.PENDING);
        this.tenantRepository.save(tenant);


    }

    @Override
    public void approveTenant(String tenantId) {

        //check if tenant exists
        final Tenant tenant = this.tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));

        //activate tenant
        tenant.setTenantStatus(TenantStatus.ACTIVE);
        this.tenantRepository.save(tenant);  //Mise à jour du statut

        try {
            // Opérations qui peuvent échouer :
            // - Création de schéma SQL
            // - Migration Flyway
            // - Création de l'user admin

            //provision the schema for the tenant
            this.provisioningService.provisionTenant(tenant);
            //Create initial admin user
            createInitialAdminUser(tenant);
        } catch (final Exception e) {
            // Si UNE de ces opérations échoue
            // → On annule TOUT
            //Si échec → retour PENDING
            rollBackTenantStatus(tenant);
        }


    }


    @Override
    public void activateTenant(String tenantId) {
        final Tenant tenant = this.tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));
        if(tenant.getTenantStatus() != TenantStatus.PENDING) {
            log.debug("Tenant is not in pending state");
            throw new InvalidRequestException("Tenant is not in pending state");
        }
        tenant.setTenantStatus(TenantStatus.ACTIVE);
        this.tenantRepository.save(tenant);
    }

    @Override
    public void deactivateTenant(String tenantId) {
        final Tenant tenant = this.tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));
        if(tenant.getTenantStatus() != TenantStatus.ACTIVE) {
            log.debug("Tenant is not in active state");
            throw new InvalidRequestException("Tenant is not in active state");
        }
        tenant.setTenantStatus(TenantStatus.INACTIVE);
        this.tenantRepository.save(tenant);
    }

    @Override
    public void suspendTenant(String tenantId) {
        final Tenant tenant = this.tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));
        if(tenant.getTenantStatus() != TenantStatus.ACTIVE) {
            log.debug("Tenant is not in active state");
            throw new InvalidRequestException("Tenant is not in active state");
        }
        tenant.setTenantStatus(TenantStatus.SUSPENDED);
        this.tenantRepository.save(tenant);
    }

    @Override
    public PageResponse<TenantResponse> findAll(int page, int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Tenant> tenants = this.tenantRepository.findAll(pageRequest);
        final Page<TenantResponse> tenantResponses = tenants.map(this.tenantMapper::toResponse);
        return PageResponse.of(tenantResponses);
    }


    private void createInitialAdminUser(Tenant tenant) {
        //check if the user already exists
        if(this.userRepository.existsByUsername(tenant.getAdminUsername())) {
            log.debug("User already exists");
            throw new DuplicateResourceException("User already exists");
        }

        //create user
        final User adminUser = User.builder()
                .username(tenant.getAdminUsername())
                .email(tenant.getAdminEmail())
                .firstName(extractFirstName(tenant.getAdminFullName()))
                .lastName(extractLastName(tenant.getAdminFullName()))
                .password(this.passwordEncoder.encode(tenant.getAdminPassword()))
                .role(UserRole.ROLE_COMPANY_ADMIN)
                .tenant(tenant)
                .enabled(true)
                .build();

        //save user
        this.userRepository.save(adminUser);
        log.debug("Created Initial admin user for tenant: {}", tenant.getId());
    }

    private String extractFirstName(final String fullName) {
        return fullName.trim().split(" ")[0];
    }

    private String extractLastName(final String fullName) {
        return fullName.trim().split(" ").length > 1 ? fullName.split(" ")[1] : fullName;
    }

    private void rollBackTenantStatus(final Tenant tenant) {
        tenant.setTenantStatus(TenantStatus.PENDING);
        this.tenantRepository.save(tenant);
    }
}
