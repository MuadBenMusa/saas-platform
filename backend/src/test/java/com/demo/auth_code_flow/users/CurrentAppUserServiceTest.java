package com.demo.auth_code_flow.users;

import com.demo.auth_code_flow.security.AuthUser;
import com.demo.auth_code_flow.tenancy.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentAppUserServiceTest {

    private AppUserRepository appUserRepository;
    private TenantMembershipRepository tenantMembershipRepository;
    private CurrentAppUserService service;
    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        tenantMembershipRepository = mock(TenantMembershipRepository.class);
        service = new CurrentAppUserService(appUserRepository, tenantMembershipRepository);
        authUser = new AuthUser(
                "keycloak-subject",
                "demo.owner@example.test",
                "Demo Owner",
                Set.of()
        );
    }

    @Test
    void resolvesFirstActiveMembershipAndTenant() {
        AppUser user = activeUser();
        Tenant tenant = activeTenant();
        TenantMembership membership = new TenantMembership(user, tenant, TenantRole.OWNER);
        when(appUserRepository.findByKeycloakSubject("keycloak-subject")).thenReturn(Optional.of(user));
        when(tenantMembershipRepository.findAllByUserIdWithTenant(user.getId()))
                .thenReturn(List.of(membership));

        CurrentAppUser result = service.resolve(authUser);

        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.tenantId()).isEqualTo(tenant.getId());
        assertThat(result.tenantName()).isEqualTo("Demo Company");
        assertThat(result.tenantRole()).isEqualTo(TenantRole.OWNER);
    }

    @Test
    void rejectsUnprovisionedKeycloakUser() {
        when(appUserRepository.findByKeycloakSubject("keycloak-subject")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolve(authUser))
                .isInstanceOf(CurrentUserNotProvisionedException.class);
    }

    @Test
    void rejectsDisabledAppUser() {
        AppUser user = activeUser();
        user.disable();
        when(appUserRepository.findByKeycloakSubject("keycloak-subject")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.resolve(authUser))
                .isInstanceOf(InactiveAppUserException.class);
    }

    @Test
    void rejectsDisabledMembership() {
        AppUser user = activeUser();
        TenantMembership membership = new TenantMembership(user, activeTenant(), TenantRole.OWNER);
        membership.disable();
        when(appUserRepository.findByKeycloakSubject("keycloak-subject")).thenReturn(Optional.of(user));
        when(tenantMembershipRepository.findAllByUserIdWithTenant(user.getId()))
                .thenReturn(List.of(membership));

        assertThatThrownBy(() -> service.resolve(authUser))
                .isInstanceOf(NoActiveTenantMembershipException.class);
    }

    @Test
    void rejectsSuspendedTenant() {
        AppUser user = activeUser();
        Tenant tenant = activeTenant();
        tenant.suspend();
        TenantMembership membership = new TenantMembership(user, tenant, TenantRole.OWNER);
        when(appUserRepository.findByKeycloakSubject("keycloak-subject")).thenReturn(Optional.of(user));
        when(tenantMembershipRepository.findAllByUserIdWithTenant(user.getId()))
                .thenReturn(List.of(membership));

        assertThatThrownBy(() -> service.resolve(authUser))
                .isInstanceOf(NoActiveTenantMembershipException.class);
    }

    private AppUser activeUser() {
        AppUser user = new AppUser(
                "keycloak-subject",
                "demo.owner@example.test",
                "Demo Owner"
        );
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private Tenant activeTenant() {
        Tenant tenant = new Tenant("Demo Company", "demo-company");
        ReflectionTestUtils.setField(tenant, "id", UUID.randomUUID());
        return tenant;
    }
}
