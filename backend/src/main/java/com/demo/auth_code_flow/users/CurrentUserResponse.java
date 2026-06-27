package com.demo.auth_code_flow.users;

import java.util.UUID;

public record CurrentUserResponse(
        UserSummary user,
        ActiveTenantSummary activeTenant
) {

    public record UserSummary(
            UUID id,
            String email,
            String name,
            AppUser.UserStatus status
    ) {
    }

    public record ActiveTenantSummary(
            UUID id,
            String name,
            String slug,
            TenantRole role
    ) {
    }
}
