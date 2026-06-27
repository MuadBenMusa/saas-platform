package com.demo.auth_code_flow.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantMembershipRepository extends JpaRepository<TenantMembership, UUID> {

    @Query("""
            select membership
            from TenantMembership membership
            join fetch membership.tenant
            where membership.user.id = :userId
            order by membership.createdAt, membership.id
            """)
    List<TenantMembership> findAllByUserIdWithTenant(@Param("userId") UUID userId);

    Optional<TenantMembership> findByUserIdAndTenantId(UUID userId, UUID tenantId);
}
