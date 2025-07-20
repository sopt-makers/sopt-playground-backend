package org.sopt.makers.internal.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.common.util.InfiniteScrollUtil;
import org.sopt.makers.internal.project.domain.Project;
import org.sopt.makers.internal.project.dto.request.ProjectSaveRequest;
import org.sopt.makers.internal.project.dto.request.ProjectUpdateRequest;
import org.sopt.makers.internal.project.dto.response.allProject.ProjectAllResponse;
import org.sopt.makers.internal.project.dto.response.allProject.ProjectResponse;
import org.sopt.makers.internal.project.dto.response.detailProject.ProjectDetailResponse;
import org.sopt.makers.internal.project.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Project 관련 API", description = "Project와 관련 API들")
public class ProjectController {
    private final ProjectService projectService;
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
        List<Project> projectList = projectService.fetchAll(infiniteScrollUtil.checkLimitForPagination(limit),
                        cursor, name, category, isAvailable, isFounding);
        List<ProjectResponse> projectResponseList = new ArrayList<>(
                projectService.getAllProjectResponseList(projectList).stream()
                        .sorted(Comparator.comparing(ProjectResponse::id).reversed())
                        .toList());
        Boolean hasNextProject = infiniteScrollUtil.checkHasNextElement(limit, projectResponseList);
        int totalProjectsCount = projectService.getProjectsCount(name, category, isAvailable, isFounding);

        ProjectAllResponse responses = new ProjectAllResponse(projectResponseList, hasNextProject, totalProjectsCount);
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
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody ProjectUpdateRequest request
    ) {
        projectService.updateProject(userId, projectId, request);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }

    @Operation(summary = "Project 삭제 API")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> delteProject (
            @PathVariable("id") Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        projectService.deleteProject(userId, projectId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }
}
