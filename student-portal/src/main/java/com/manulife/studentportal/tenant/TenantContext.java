package com.manulife.studentportal.tenant;

/**
 * Holds the current tenant identifier for the executing thread.
 * Set by TenantFilter on every request and read by Hibernate's
 * CurrentTenantIdentifierResolver to route queries to the correct schema.
 */
public final class TenantContext {

    public static final String DEFAULT_TENANT = "student_portal";

    private static final ThreadLocal<String> CURRENT_TENANT = ThreadLocal.withInitial(() -> DEFAULT_TENANT);

    private TenantContext() {
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
