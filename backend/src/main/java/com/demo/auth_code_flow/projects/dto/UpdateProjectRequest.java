package com.demo.auth_code_flow.projects.dto;

import com.demo.auth_code_flow.projects.ProjectStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        @Size(max = 60)
        String projectNumber,

        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        @Size(max = 200)
        String name,

        @Size(max = 2000)
        String description,

        ProjectStatus status
) {
}
