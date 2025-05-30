package org.sopt.makers.internal.community.dto.response;

import java.util.List;

public record PostAllResponse(
        Long categoryId,
        Boolean hasNext,
        List<PostResponse> posts
) {}