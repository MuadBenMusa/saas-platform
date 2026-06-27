package com.demo.auth_code_flow.projects;

import com.demo.auth_code_flow.projects.dto.CreateProjectRequest;
import com.demo.auth_code_flow.projects.dto.ProjectResponse;
import com.demo.auth_code_flow.projects.dto.UpdateProjectRequest;
import com.demo.auth_code_flow.security.AuthUser;
import com.demo.auth_code_flow.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final CurrentUserService currentUserService;
    private final ProjectService projectService;

    public ProjectController(
            CurrentUserService currentUserService,
            ProjectService projectService
    ) {
        this.currentUserService = currentUserService;
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> list(OAuth2AuthenticationToken token) {
        return projectService.list(currentUserService.from(token));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(
            OAuth2AuthenticationToken token,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        AuthUser authUser = currentUserService.from(token);
        return projectService.create(authUser, request);
    }

    @GetMapping("/{id}")
    public ProjectResponse get(
            OAuth2AuthenticationToken token,
            @PathVariable UUID id
    ) {
        return projectService.get(currentUserService.from(token), id);
    }

    @PatchMapping("/{id}")
    public ProjectResponse update(
            OAuth2AuthenticationToken token,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        AuthUser authUser = currentUserService.from(token);
        return projectService.update(authUser, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(
            OAuth2AuthenticationToken token,
            @PathVariable UUID id
    ) {
        projectService.archive(currentUserService.from(token), id);
    }
}
