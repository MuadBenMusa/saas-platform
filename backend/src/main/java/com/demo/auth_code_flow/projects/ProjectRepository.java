package com.demo.auth_code_flow.projects;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByTenant_IdOrderByCreatedAtDesc(UUID tenantId);

    Optional<Project> findByIdAndTenant_Id(UUID id, UUID tenantId);

    boolean existsByTenant_IdAndProjectNumber(UUID tenantId, String projectNumber);

    boolean existsByTenant_IdAndProjectNumberAndIdNot(
            UUID tenantId,
            String projectNumber,
            UUID projectId
    );
}
