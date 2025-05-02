package org.sopt.makers.internal.project.dto.response;

import java.util.List;

public record ProjectAllResponse(
        List<ProjectResponse> projectList,
        Boolean hasNext,
        Integer totalCount
){}
