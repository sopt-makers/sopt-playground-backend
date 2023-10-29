package org.sopt.makers.internal.dto.community;

import java.util.List;

public record PostAllResponse(
        Long categoryId,
        Boolean hasNext,
        List<PostResponse> posts
) {}