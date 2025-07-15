package org.sopt.makers.internal.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.common.util.InfiniteScrollUtil;
import org.sopt.makers.internal.internal.InternalMemberDetails;
import org.sopt.makers.internal.project.domain.Project;
import org.sopt.makers.internal.project.dto.response.*;
import org.sopt.makers.internal.project.mapper.ProjectResponseMapper;
import org.sopt.makers.internal.project.dto.request.ProjectSaveRequest;
import org.sopt.makers.internal.project.dto.request.ProjectUpdateRequest;
import org.sopt.makers.internal.project.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    private final ProjectResponseMapper projectMapper;
    private final InfiniteScrollUtil infiniteScrollUtil;

    @Operation(summary = "Project id로 조회 API")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailResponse> getProject (@PathVariable Long id) {
        ProjectDetailResponse response = projectService.getProjectDetailResponseById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Project 전체 조회 API",
            description = "cursor : 처음에는 null 또는 0, 이후 마지막으로 조회된 project id"
    )
    @GetMapping("")
    public ResponseEntity<ProjectAllResponse> getProjects (
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Long cursor,
            @RequestParam(required = false, name = "name") String name,
            @RequestParam(required = false, name = "category") String category,
            @RequestParam(required = false, name = "isAvailable") Boolean isAvailable,
            @RequestParam(required = false, name = "isFounding") Boolean isFounding
    ) {
        val projectMap = projectService.fetchAll(infiniteScrollUtil.checkLimitForPagination(limit), cursor, name, category, isAvailable, isFounding)
                .stream().collect(Collectors.toMap(Project::getId, Function.identity()));
        val projectLinkMap = projectService.fetchAllLinks().stream()
                .collect(Collectors.groupingBy(ProjectLinkDao::id, Collectors.toList()));
        val projectIds = projectMap.keySet();
        val projectList = projectIds.stream().sorted(Collections.reverseOrder())
                .map(id -> projectMapper.toProjectResponse(projectMap.get(id), projectService.fetchById(id), projectLinkMap.getOrDefault(id, List.of())))
                .collect(Collectors.toList());
        val hasNextProject = infiniteScrollUtil.checkHasNextElement(limit, projectList);
        val totalProjectsCount = projectService.getProjectsCount(name, category, isAvailable, isFounding);
        val responses = new ProjectAllResponse(projectList, hasNextProject, totalProjectsCount);
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
        Long writerId = memberDetails.getId();
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
}
