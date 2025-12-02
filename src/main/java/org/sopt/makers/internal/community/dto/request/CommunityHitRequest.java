package org.sopt.makers.internal.community.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CommunityHitRequest(

        @NotNull List<Long> postIdList
) {
}
