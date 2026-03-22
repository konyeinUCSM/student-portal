package com.manulife.studentportal.tenant.internal;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.manulife.studentportal.tenant.TenantContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Extracts the tenant identifier from the X-Tenant-ID request header
 * and sets it in the TenantContext for the current thread.
 * Must run before the JwtAuthenticationFilter so the schema is set
 * before any DB access.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantId = request.getHeader(TENANT_HEADER);

        if (tenantId != null && !tenantId.isBlank()) {
            TenantContext.setCurrentTenant(tenantId.trim());
            log.debug("Tenant set to: {}", tenantId.trim());
        } else {
            TenantContext.setCurrentTenant(TenantContext.DEFAULT_TENANT);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
