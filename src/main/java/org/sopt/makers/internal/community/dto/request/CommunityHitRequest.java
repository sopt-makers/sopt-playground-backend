package org.sopt.makers.internal.community.dto.request;

import javax.validation.constraints.NotNull;
import java.util.List;

public record CommunityHitRequest(

        @NotNull List<Long> postIdList
) {
}
