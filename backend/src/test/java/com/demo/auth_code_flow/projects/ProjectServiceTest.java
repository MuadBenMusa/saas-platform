package com.demo.auth_code_flow.projects;

import com.demo.auth_code_flow.audit.AuditService;
import com.demo.auth_code_flow.projects.dto.CreateProjectRequest;
import com.demo.auth_code_flow.projects.dto.ProjectResponse;
import com.demo.auth_code_flow.projects.dto.UpdateProjectRequest;
import com.demo.auth_code_flow.security.AuthUser;
import com.demo.auth_code_flow.tenancy.Tenant;
import com.demo.auth_code_flow.tenancy.TenantRepository;
import com.demo.auth_code_flow.users.AppUser;
import com.demo.auth_code_flow.users.CurrentAppUser;
import com.demo.auth_code_flow.users.CurrentAppUserService;
import com.demo.auth_code_flow.users.TenantRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectServiceTest {

    private ProjectRepository projectRepository;
    private TenantRepository tenantRepository;
    private CurrentAppUserService currentAppUserService;
    private AuditService auditService;
    private ProjectService service;
    private AuthUser authUser;
    private UUID tenantId;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        tenantRepository = mock(TenantRepository.class);
        currentAppUserService = mock(CurrentAppUserService.class);
        auditService = mock(AuditService.class);
        service = new ProjectService(
                projectRepository,
                tenantRepository,
                currentAppUserService,
                auditService
        );

        authUser = new AuthUser("keycloak-subject", "owner@example.test", "Owner", Set.of());
        tenantId = UUID.randomUUID();
        tenant = new Tenant("Demo Company", "demo-company");
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        when(currentAppUserService.resolve(authUser)).thenReturn(currentAppUser(tenantId));
    }

    @Test
    void createUsesActiveTenantAndDoesNotAcceptTenantFromRequest() {
        when(tenantRepository.getReferenceById(tenantId)).thenReturn(tenant);
        when(projectRepository.existsByTenant_IdAndProjectNumber(tenantId, "PRJ-001"))
                .thenReturn(false);
        when(projectRepository.saveAndFlush(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(project, "createdAt", Instant.parse("2026-06-21T10:00:00Z"));
            ReflectionTestUtils.setField(project, "updatedAt", Instant.parse("2026-06-21T10:00:00Z"));
            return project;
        });

        ProjectResponse response = service.create(
                authUser,
                new CreateProjectRequest("  PRJ-001  ", "  First Project  ", "  Demo  ")
        );

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).saveAndFlush(projectCaptor.capture());
        assertThat(projectCaptor.getValue().getTenant()).isSameAs(tenant);
        assertThat(response.tenantId()).isEqualTo(tenantId);
        assertThat(response.projectNumber()).isEqualTo("PRJ-001");
        assertThat(response.name()).isEqualTo("First Project");
        assertThat(response.description()).isEqualTo("Demo");
        verify(auditService).record(
                "keycloak-subject",
                "PROJECT_CREATED",
                "PROJECT",
                response.id().toString()
        );
    }

    @Test
    void listReturnsOnlyProjectsQueriedForActiveTenant() {
        Project project = project(tenant, "PRJ-001");
        when(projectRepository.findAllByTenant_IdOrderByCreatedAtDesc(tenantId))
                .thenReturn(List.of(project));

        List<ProjectResponse> response = service.list(authUser);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().tenantId()).isEqualTo(tenantId);
        verify(projectRepository).findAllByTenant_IdOrderByCreatedAtDesc(tenantId);
    }

    @Test
    void getTreatsProjectFromAnotherTenantAsNotFound() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findByIdAndTenant_Id(projectId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(authUser, projectId))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void updateTreatsProjectFromAnotherTenantAsNotFound() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findByIdAndTenant_Id(projectId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(
                authUser,
                projectId,
                new UpdateProjectRequest(null, "Changed", null, null)
        )).isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository, never()).flush();
    }

    @Test
    void archiveTreatsProjectFromAnotherTenantAsNotFound() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findByIdAndTenant_Id(projectId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.archive(authUser, projectId))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository, never()).flush();
    }

    private CurrentAppUser currentAppUser(UUID activeTenantId) {
        return new CurrentAppUser(
                UUID.randomUUID(),
                "owner@example.test",
                "Owner",
                AppUser.UserStatus.ACTIVE,
                activeTenantId,
                "Demo Company",
                "demo-company",
                TenantRole.OWNER
        );
    }

    private Project project(Tenant projectTenant, String projectNumber) {
        Project project = new Project(projectTenant, projectNumber, "Project", null);
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(project, "createdAt", Instant.parse("2026-06-21T10:00:00Z"));
        ReflectionTestUtils.setField(project, "updatedAt", Instant.parse("2026-06-21T10:00:00Z"));
        return project;
    }
}
