package com.demo.auth_code_flow.projects.dto;

import com.demo.auth_code_flow.projects.ProjectStatus;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String projectNumber,
        String name,
        String description,
        ProjectStatus status,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt
) {
}
