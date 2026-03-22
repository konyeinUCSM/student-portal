package com.manulife.studentportal.tenant.internal;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.shared.exception.DuplicateResourceException;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
import com.manulife.studentportal.tenant.TenantContext;
import com.manulife.studentportal.tenant.TenantService;
import com.manulife.studentportal.tenant.web.CreateTenantRequest;
import com.manulife.studentportal.tenant.web.TenantResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantSchemaInitializer schemaInitializer;

    @Override
    public TenantResponse create(CreateTenantRequest request) {
        log.info("Creating tenant: name={}, schema={}", request.getName(), request.getSchemaName());

        if (tenantRepository.existsBySchemaName(request.getSchemaName())) {
            throw new DuplicateResourceException("Tenant with schema '" + request.getSchemaName() + "' already exists");
        }

        if (TenantContext.DEFAULT_TENANT.equals(request.getSchemaName())) {
            throw new DuplicateResourceException("Cannot use the default schema name as a tenant schema");
        }

        // Create the MySQL schema
        schemaInitializer.initializeSchema(request.getSchemaName());

        // Register in the tenants table
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .schemaName(request.getSchemaName())
                .build();

        tenant = tenantRepository.save(tenant);
        log.info("Tenant created: id={}, schema={}", tenant.getId(), tenant.getSchemaName());

        return toResponse(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> getAll() {
        return tenantRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
        return toResponse(tenant);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .schemaName(tenant.getSchemaName())
                .active(tenant.isActive())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
