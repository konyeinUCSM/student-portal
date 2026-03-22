package com.manulife.studentportal.tenant;

import java.util.List;

import com.manulife.studentportal.tenant.web.CreateTenantRequest;
import com.manulife.studentportal.tenant.web.TenantResponse;

public interface TenantService {

    TenantResponse create(CreateTenantRequest request);

    List<TenantResponse> getAll();

    TenantResponse getById(Long id);
}
