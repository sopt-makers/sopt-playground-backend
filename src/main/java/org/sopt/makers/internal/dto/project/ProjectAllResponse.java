package org.sopt.makers.internal.dto.project;

import java.util.List;

public record ProjectAllResponse(
        List<ProjectResponse> projectList,
        Boolean hasNext,
        Integer totalCount
){}
