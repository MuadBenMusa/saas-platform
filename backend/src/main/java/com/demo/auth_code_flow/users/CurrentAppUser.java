package com.demo.auth_code_flow.users;

import java.util.UUID;

public record CurrentAppUser(
        UUID userId,
        String email,
        String name,
        AppUser.UserStatus userStatus,
        UUID tenantId,
        String tenantName,
        String tenantSlug,
        TenantRole tenantRole
) {
}
