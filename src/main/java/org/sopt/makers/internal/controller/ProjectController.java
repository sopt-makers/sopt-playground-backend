package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.dto.project.ProjectDao;
import org.sopt.makers.internal.dto.project.ProjectMemberDao;
import org.sopt.makers.internal.dto.project.ProjectLinkDao;
import org.sopt.makers.internal.dto.project.ProjectResponse;
import org.sopt.makers.internal.dto.project.ProjectSaveRequest;
import org.sopt.makers.internal.dto.project.ProjectUpdateRequest;
import org.sopt.makers.internal.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        val projectMembers = projectService.fetchById(id);
        val projectLinks = projectService.fetchLinksById(id);
        val response = toProjectResponse(projectMembers, projectLinks);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Project 전체 조회 API")
    @GetMapping("")
    public ResponseEntity<List<ProjectResponse>> getProjects () {
        val projectMemberMap = projectService.fetchAll().stream()
                .collect(Collectors.groupingBy(ProjectMemberDao::id, Collectors.toList()));
        val projectLinkMap = projectService.fetchAllLinks().stream()
                .collect(Collectors.groupingBy(ProjectLinkDao::id, Collectors.toList()));
        val projectIds = projectMemberMap.keySet();
        val responses = projectIds.stream()
                .map(id -> toProjectResponse(
                        projectMemberMap.get(id),
                        projectLinkMap.getOrDefault(id, List.of())))
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
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody ProjectUpdateRequest request
    ) {
        val writerId = memberDetails.getId();
        projectService.updateProject(writerId, projectId, request);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }

    private ProjectResponse.ProjectMemberResponse toProjectMemberResponse (ProjectDao project) {
        return new ProjectResponse.ProjectMemberResponse(
                project.memberId(), project.memberRole(), project.memberDesc(), project.isTeamMember(),
                project.memberName(), project.memberGeneration()
        );
    }

    private ProjectResponse.ProjectMemberResponse toProjectMemberResponse (ProjectMemberDao project) {
        return new ProjectResponse.ProjectMemberResponse(
                project.memberId(), project.memberRole(), project.memberDesc(), project.isTeamMember(),
                project.memberName(), project.memberGeneration()
        );
    }

    private ProjectResponse.ProjectLinkResponse toProjectLinkResponse (ProjectLinkDao project) {
        return new ProjectResponse.ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }

    private ProjectResponse.ProjectLinkResponse toProjectLinkResponse (ProjectDao project) {
        return new ProjectResponse.ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }

    private ProjectResponse toProjectResponse (List<ProjectMemberDao> projectMembers, List<ProjectLinkDao> projectLinks) {
        val projectInfo = projectMembers.get(0);
        val memberResponses = projectMembers.stream().map(this::toProjectMemberResponse).collect(Collectors.toList());
        val linkResponses = projectLinks.stream().map(this::toProjectLinkResponse).collect(Collectors.toList());

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
