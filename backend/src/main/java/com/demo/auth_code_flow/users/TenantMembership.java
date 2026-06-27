package com.demo.auth_code_flow.users;

import com.demo.auth_code_flow.tenancy.Tenant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "tenant_memberships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tenant_memberships_user_tenant",
                        columnNames = {"user_id", "tenant_id"}
                )
        },
        indexes = {
                @Index(name = "idx_tenant_memberships_user", columnList = "user_id"),
                @Index(name = "idx_tenant_memberships_tenant", columnList = "tenant_id")
        }
)
public class TenantMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TenantMembershipStatus status = TenantMembershipStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TenantRole role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected TenantMembership() {
    }

    public TenantMembership(AppUser user, Tenant tenant, TenantRole role) {
        this.user = user;
        this.tenant = tenant;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public TenantMembershipStatus getStatus() {
        return status;
    }

    public TenantRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void disable() {
        this.status = TenantMembershipStatus.DISABLED;
    }
}
