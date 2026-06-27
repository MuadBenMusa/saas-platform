package com.demo.auth_code_flow.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank
        @Size(max = 60)
        String projectNumber,

        @NotBlank
        @Size(max = 200)
        String name,

        @Size(max = 2000)
        String description
) {
}
