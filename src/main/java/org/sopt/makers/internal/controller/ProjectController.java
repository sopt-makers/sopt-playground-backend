package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.domain.Project;
import org.sopt.makers.internal.dto.project.*;
import org.sopt.makers.internal.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    public ResponseEntity<ProjectDetailResponse> getProject (@PathVariable Long id) {
        val projectMembers = projectService.fetchById(id);
        val projectLinks = projectService.fetchLinksById(id);
        val response = toProjectDetailResponse(projectMembers, projectLinks);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Project 전체 조회 API")
    @GetMapping("")
    public ResponseEntity<List<ProjectResponse>> getProjects () {
        val projectMap = projectService.fetchAll().stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));
        val projectLinkMap = projectService.fetchAllLinks().stream()
                .collect(Collectors.groupingBy(ProjectLinkDao::id, Collectors.toList()));
        val projectIds = projectMap.keySet();
        val responses = projectIds.stream()
                .map(id -> toProjectResponse(projectMap.get(id), projectLinkMap.getOrDefault(id, List.of())))
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

    @Operation(summary = "Project 삭제 API")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> delteProject (
            @PathVariable("id") Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val writerId = memberDetails.getId();
        projectService.deleteProject(writerId, projectId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }

    private ProjectDetailResponse.ProjectMemberResponse toProjectMemberResponse (ProjectMemberVo project) {
        return new ProjectDetailResponse.ProjectMemberResponse(
                project.memberId(), project.memberRole(), project.memberDesc(), project.isTeamMember(),
                project.memberName(), project.memberGenerations(), project.memberProfileImage(), project.memberHasProfile()
        );
    }

    private ProjectResponse.ProjectLinkResponse toProjectLinkResponse (ProjectLinkDao project) {
        return new ProjectResponse.ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }

    private ProjectDetailResponse.ProjectLinkResponse toProjectDetailLinkResponse (ProjectLinkDao project) {
        return new ProjectDetailResponse.ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }
    private ProjectResponse toProjectResponse (Project project, List<ProjectLinkDao> projectLinks) {
        val linkResponses = projectLinks.stream().map(this::toProjectLinkResponse).collect(Collectors.toList());

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getGeneration(),
                project.getCategory(),
                project.getServiceType(),
                project.getSummary(),
                project.getDetail(),
                project.getLogoImage(),
                project.getThumbnailImage(),
                linkResponses
        );
    }

    private ProjectDetailResponse toProjectDetailResponse (List<ProjectMemberVo> projectMembers, List<ProjectLinkDao> projectLinks) {
        val projectInfo = projectMembers.get(0);
        val memberResponses = projectMembers.stream().map(this::toProjectMemberResponse).collect(Collectors.toList());
        val linkResponses = projectLinks.stream().map(this::toProjectDetailLinkResponse).collect(Collectors.toList());

        return new ProjectDetailResponse(
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
