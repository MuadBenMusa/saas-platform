package com.demo.auth_code_flow.projects;

import com.demo.auth_code_flow.audit.AuditService;
import com.demo.auth_code_flow.projects.dto.CreateProjectRequest;
import com.demo.auth_code_flow.projects.dto.ProjectResponse;
import com.demo.auth_code_flow.projects.dto.UpdateProjectRequest;
import com.demo.auth_code_flow.security.AuthUser;
import com.demo.auth_code_flow.tenancy.Tenant;
import com.demo.auth_code_flow.tenancy.TenantRepository;
import com.demo.auth_code_flow.users.CurrentAppUser;
import com.demo.auth_code_flow.users.CurrentAppUserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TenantRepository tenantRepository;
    private final CurrentAppUserService currentAppUserService;
    private final AuditService auditService;

    public ProjectService(
            ProjectRepository projectRepository,
            TenantRepository tenantRepository,
            CurrentAppUserService currentAppUserService,
            AuditService auditService
    ) {
        this.projectRepository = projectRepository;
        this.tenantRepository = tenantRepository;
        this.currentAppUserService = currentAppUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(AuthUser authUser) {
        // Tenant ID is resolved from the server-side context, never accepted from the frontend request.
        UUID tenantId = currentAppUserService.resolve(authUser).tenantId();
        return projectRepository.findAllByTenant_IdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(ProjectService::toResponse)
                .toList();
    }

    @Transactional
    public ProjectResponse create(AuthUser authUser, CreateProjectRequest request) {
        CurrentAppUser currentUser = currentAppUserService.resolve(authUser);
        String projectNumber = normalizeRequired(request.projectNumber());

        if (projectRepository.existsByTenant_IdAndProjectNumber(
                currentUser.tenantId(),
                projectNumber
        )) {
            throw new DuplicateProjectNumberException(projectNumber);
        }

        Tenant tenant = tenantRepository.getReferenceById(currentUser.tenantId());
        Project project = new Project(
                tenant,
                projectNumber,
                normalizeRequired(request.name()),
                normalizeOptional(request.description())
        );

        Project savedProject;
        try {
            savedProject = projectRepository.saveAndFlush(project);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateProjectNumberException(projectNumber);
        }

        auditService.record(
                authUser.subject(),
                "PROJECT_CREATED",
                "PROJECT",
                savedProject.getId().toString()
        );
        return toResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(AuthUser authUser, UUID projectId) {
        UUID tenantId = currentAppUserService.resolve(authUser).tenantId();
        return toResponse(findForTenant(projectId, tenantId));
    }

    @Transactional
    public ProjectResponse update(
            AuthUser authUser,
            UUID projectId,
            UpdateProjectRequest request
    ) {
        CurrentAppUser currentUser = currentAppUserService.resolve(authUser);
        Project project = findForTenant(projectId, currentUser.tenantId());

        String projectNumber = request.projectNumber() == null
                ? project.getProjectNumber()
                : normalizeRequired(request.projectNumber());
        if (!projectNumber.equals(project.getProjectNumber())
                && projectRepository.existsByTenant_IdAndProjectNumberAndIdNot(
                        currentUser.tenantId(),
                        projectNumber,
                        projectId
                )) {
            throw new DuplicateProjectNumberException(projectNumber);
        }

        project.update(
                projectNumber,
                request.name() == null ? project.getName() : normalizeRequired(request.name()),
                request.description() == null
                        ? project.getDescription()
                        : normalizeOptional(request.description()),
                request.status() == null ? project.getStatus() : request.status()
        );

        try {
            projectRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateProjectNumberException(projectNumber);
        }

        auditService.record(
                authUser.subject(),
                "PROJECT_UPDATED",
                "PROJECT",
                project.getId().toString()
        );
        return toResponse(project);
    }

    @Transactional
    public void archive(AuthUser authUser, UUID projectId) {
        UUID tenantId = currentAppUserService.resolve(authUser).tenantId();
        Project project = findForTenant(projectId, tenantId);
        project.archive();
        projectRepository.flush();

        auditService.record(
                authUser.subject(),
                "PROJECT_ARCHIVED",
                "PROJECT",
                project.getId().toString()
        );
    }

    private Project findForTenant(UUID projectId, UUID tenantId) {
        // We throw a 404 (Not Found) instead of 403 (Forbidden) for cross-tenant access attempts.
        // This prevents leaking the existence of other tenants' data (IDOR protection).
        return projectRepository.findByIdAndTenant_Id(projectId, tenantId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    private static ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getProjectNumber(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getTenant().getId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private static String normalizeRequired(String value) {
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
