package com.demo.auth_code_flow.users;

import com.demo.auth_code_flow.security.AuthUser;
import com.demo.auth_code_flow.tenancy.Tenant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CurrentAppUserService {

    private final AppUserRepository appUserRepository;
    private final TenantMembershipRepository tenantMembershipRepository;

    public CurrentAppUserService(
            AppUserRepository appUserRepository,
            TenantMembershipRepository tenantMembershipRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.tenantMembershipRepository = tenantMembershipRepository;
    }

    @Transactional(readOnly = true)
    public CurrentAppUser resolve(AuthUser authUser) {
        if (authUser == null || !StringUtils.hasText(authUser.subject())) {
            throw new CurrentUserNotProvisionedException();
        }

        AppUser appUser = appUserRepository.findByKeycloakSubject(authUser.subject())
                .orElseThrow(CurrentUserNotProvisionedException::new);

        if (appUser.getStatus() != AppUser.UserStatus.ACTIVE) {
            throw new InactiveAppUserException();
        }

        // Tenant context is explicitly resolved server-side from the authenticated user's active membership.
        // We never trust a tenant ID provided by the frontend to prevent tenant spoofing attacks.
        TenantMembership activeMembership = tenantMembershipRepository
                .findAllByUserIdWithTenant(appUser.getId())
                .stream()
                .filter(membership -> membership.getStatus() == TenantMembershipStatus.ACTIVE)
                .filter(membership -> membership.getTenant().getStatus() == Tenant.TenantStatus.ACTIVE)
                .findFirst()
                .orElseThrow(NoActiveTenantMembershipException::new);

        Tenant tenant = activeMembership.getTenant();
        return new CurrentAppUser(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getName(),
                appUser.getStatus(),
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                activeMembership.getRole()
        );
    }
}
