package com.demo.auth_code_flow.projects;

import com.demo.auth_code_flow.audit.AuditService;
import com.demo.auth_code_flow.projects.dto.CreateProjectRequest;
import com.demo.auth_code_flow.projects.dto.ProjectResponse;
import com.demo.auth_code_flow.security.AuthUser;
import com.demo.auth_code_flow.tenancy.Tenant;
import com.demo.auth_code_flow.tenancy.TenantRepository;
import com.demo.auth_code_flow.users.AppUser;
import com.demo.auth_code_flow.users.AppUserRepository;
import com.demo.auth_code_flow.users.CurrentAppUserService;
import com.demo.auth_code_flow.users.TenantMembership;
import com.demo.auth_code_flow.users.TenantMembershipRepository;
import com.demo.auth_code_flow.users.TenantRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        ProjectService.class,
        CurrentAppUserService.class,
        AuditService.class,
        ProjectTenantIsolationIntegrationTest.PostgresTestConfiguration.class
})
class ProjectTenantIsolationIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TenantMembershipRepository tenantMembershipRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PostgreSQLContainer postgresContainer;

    @Test
    void isolatesProjectsByActiveTenantAndScopesProjectNumberUniqueness() throws SQLException {
        assertPostgreSqlContainerAndFlywayMigration();

        Tenant tenantA = tenantRepository.saveAndFlush(new Tenant("Tenant A", "tenant-a"));
        Tenant tenantB = tenantRepository.saveAndFlush(new Tenant("Tenant B", "tenant-b"));

        AppUser userA = appUserRepository.saveAndFlush(
                new AppUser("user-a", "user-a@example.test", "User A")
        );
        AppUser userB = appUserRepository.saveAndFlush(
                new AppUser("user-b", "user-b@example.test", "User B")
        );
        AppUser userC = appUserRepository.saveAndFlush(
                new AppUser("user-c", "user-c@example.test", "User C")
        );

        tenantMembershipRepository.saveAllAndFlush(List.of(
                new TenantMembership(userA, tenantA, TenantRole.OWNER),
                new TenantMembership(userB, tenantA, TenantRole.MEMBER),
                new TenantMembership(userC, tenantB, TenantRole.OWNER)
        ));

        AuthUser authUserA = authUser(userA);
        AuthUser authUserB = authUser(userB);
        AuthUser authUserC = authUser(userC);

        ProjectResponse tenantAProject = projectService.create(
                authUserA,
                new CreateProjectRequest("PRJ-001", "Tenant A Project", null)
        );

        assertThat(projectService.list(authUserB))
                .extracting(ProjectResponse::id)
                .containsExactly(tenantAProject.id());
        assertThat(projectService.get(authUserB, tenantAProject.id()).tenantId())
                .isEqualTo(tenantA.getId());

        assertThat(projectService.list(authUserC)).isEmpty();
        assertThatThrownBy(() -> projectService.get(authUserC, tenantAProject.id()))
                .isInstanceOf(ProjectNotFoundException.class);

        ProjectResponse tenantBProject = projectService.create(
                authUserC,
                new CreateProjectRequest("PRJ-001", "Tenant B Project", null)
        );

        assertThat(tenantBProject.tenantId()).isEqualTo(tenantB.getId());
        assertThat(tenantBProject.projectNumber()).isEqualTo("PRJ-001");
        assertThat(tenantBProject.id()).isNotEqualTo(tenantAProject.id());

        assertThatThrownBy(() -> projectService.create(
                authUserA,
                new CreateProjectRequest("PRJ-001", "Duplicate Tenant A Project", null)
        )).isInstanceOf(DuplicateProjectNumberException.class);
    }

    private void assertPostgreSqlContainerAndFlywayMigration() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
            assertThat(connection.getMetaData().getURL()).isEqualTo(postgresContainer.getJdbcUrl());
        }

        assertThat(jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '1'
                          AND success
                        """,
                Long.class
        )).isEqualTo(1L);
    }

    private AuthUser authUser(AppUser appUser) {
        return new AuthUser(
                appUser.getKeycloakSubject(),
                appUser.getEmail(),
                appUser.getName(),
                Set.of()
        );
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class PostgresTestConfiguration {

        // Using a real PostgreSQL container ensures that tenant isolation queries and constraints
        // behave exactly as they will in production, avoiding H2-specific quirks.
        @Bean
        @ServiceConnection
        PostgreSQLContainer postgresContainer() {
            return new PostgreSQLContainer("postgres:16")
                    .withDatabaseName("tenant_isolation_test")
                    .withUsername("test")
                    .withPassword("test");
        }
    }
}
