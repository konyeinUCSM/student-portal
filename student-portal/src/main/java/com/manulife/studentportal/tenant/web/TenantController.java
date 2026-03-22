package com.manulife.studentportal.tenant.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manulife.studentportal.shared.annotation.AdminOnly;
import com.manulife.studentportal.shared.dto.ApiResponse;
import com.manulife.studentportal.tenant.TenantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@AdminOnly
@Tag(name = "Tenant Management", description = "APIs for managing tenants (Admin only)")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @Operation(summary = "Create a new tenant", description = "Creates a new tenant with its own database schema")
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(
            @Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = tenantService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tenant created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all tenants", description = "Retrieves all registered tenants")
    public ResponseEntity<ApiResponse<List<TenantResponse>>> getAllTenants() {
        List<TenantResponse> tenants = tenantService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Tenants retrieved successfully", tenants));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tenant by ID", description = "Retrieves a specific tenant by ID")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(@PathVariable Long id) {
        TenantResponse response = tenantService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant retrieved successfully", response));
    }
}
