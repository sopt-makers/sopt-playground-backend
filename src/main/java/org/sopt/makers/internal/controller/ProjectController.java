package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.dto.project.ProjectDao;
import org.sopt.makers.internal.dto.project.ProjectResponse;
import org.sopt.makers.internal.dto.project.ProjectSaveRequest;
import org.sopt.makers.internal.dto.project.ProjectUpdateRequest;
import org.sopt.makers.internal.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Project 관련 API", description = "Project와 관련 API들")
public class ProjectController {
    private final ProjectService projectService;

    @Operation(summary = "Project id로 조회 API")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject (@PathVariable Long id) {
        val project = projectService.fetchById(id);
        val response = toProjectResponse(project);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Project 전체 조회 API")
    @GetMapping("")
    public ResponseEntity<List<ProjectResponse>> getProjects () {
        val projects = projectService.fetchAll();
        val responses = projects.stream()
                .collect(Collectors.groupingBy(ProjectDao::id, Collectors.toList()))
                .values().stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @Operation(summary = "Project 생성 API")
    @PostMapping("")
    public ResponseEntity<Map<String, Boolean>> createProject (@RequestBody ProjectSaveRequest request) {
        projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true));
    }

    @Operation(summary = "Project 수정 API")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> updateProject (
            @PathVariable("id") Long projectId,
            @RequestBody ProjectUpdateRequest request
    ) {
        projectService.updateProject(projectId, request);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }

    private ProjectResponse.ProjectMemberResponse toProjectMemberResponse (ProjectDao project) {
        return new ProjectResponse.ProjectMemberResponse(
                project.memberId(), project.memberRole(), project.memberDesc(), project.isTeamMember(),
                project.memberName(), project.memberGeneration()
        );
    }

    private ProjectResponse.ProjectLinkResponse toProjectLinkResponse (ProjectDao project) {
        return new ProjectResponse.ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }

    private ProjectResponse toProjectResponse (List<ProjectDao> project) {
        val projectInfo = project.get(0);
        val memberResponses = project.stream().map(this::toProjectMemberResponse).collect(Collectors.toList());
        val linkResponses = project.stream().map(this::toProjectLinkResponse).collect(Collectors.toList());

        return new ProjectResponse(
                projectInfo.id(),
                projectInfo.name(),
                projectInfo.writerId(),
                projectInfo.generation(),
                projectInfo.category(),
                projectInfo.startAt(),
                projectInfo.endAt(),
                projectInfo.serviceType(),
                projectInfo.isAvailable(),
                projectInfo.isFounding(),
                projectInfo.summary(),
                projectInfo.detail(),
                projectInfo.logoImage(),
                projectInfo.thumbnailImage(),
                projectInfo.images(),
                projectInfo.createdAt(),
                projectInfo.updatedAt(),
                memberResponses,
                linkResponses
        );
    }
}
