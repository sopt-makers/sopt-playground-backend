package org.sopt.makers.internal.dto.community;

import javax.validation.constraints.NotNull;
import java.util.List;

public record CommunityHitRequest(

        @NotNull List<Long> postIdList
) {
}
