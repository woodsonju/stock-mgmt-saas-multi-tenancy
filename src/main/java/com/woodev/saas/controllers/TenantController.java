package com.woodev.saas.controllers;

import com.woodev.saas.common.PageResponse;
import com.woodev.saas.requests.RegisterTenantRequest;
import com.woodev.saas.responses.TenantResponse;
import com.woodev.saas.services.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/approve/{tenant-id}")
    public ResponseEntity<Void> approveTenant(
            @PathVariable("tenant-id")
            final String tenantId){
        this.tenantService.approveTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    //PatchMapping car on veut changer une partie de notre objet tenant
    @PatchMapping("/activate/{tenant-id}")
    public ResponseEntity<Void> activateTenant(
            @PathVariable("tenant-id")
            final String tenantId){
        this.tenantService.activateTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/deactivate/{tenant-id}")
    public ResponseEntity<Void> deactivateTenant(
            @PathVariable("tenant-id")
            final String tenantId){
        this.tenantService.deactivateTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/suspend/{tenant-id}")
    public ResponseEntity<Void> suspendTenant(
            @PathVariable("tenant-id")
            final String tenantId){
        this.tenantService.suspendTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<TenantResponse>> findAllTenants(
            @RequestParam(name="page", defaultValue = "0") final int page,
            @RequestParam(name="size", defaultValue = "10")
            final int size
    ){
        return ResponseEntity.ok(this.tenantService.findAll(page, size));
    }

}
