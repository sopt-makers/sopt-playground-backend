package org.sopt.makers.internal.external.makers;

import java.util.List;

public record CrewPostListResponse(
        List<CrewPost> posts,
        PaginationMeta pageMeta
) {
}
