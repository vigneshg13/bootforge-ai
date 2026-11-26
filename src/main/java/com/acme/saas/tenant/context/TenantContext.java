package com.acme.saas.tenant.context;

import com.acme.saas.tenant.domain.Role;
import java.util.UUID;

public final class TenantContext {
    public record TenantInfo(UUID organizationId, UUID userId, Role role) {}

    private static final ThreadLocal<TenantInfo> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(TenantInfo info) {
        CURRENT.set(info);
    }

    public static TenantInfo get() {
        return CURRENT.get();
    }

    public static UUID requireOrgId() {
        TenantInfo info = CURRENT.get();
        if (info == null || info.organizationId() == null) {
            throw new IllegalStateException("Missing X-Tenant-Id header or tenant context");
        }
        return info.organizationId();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
