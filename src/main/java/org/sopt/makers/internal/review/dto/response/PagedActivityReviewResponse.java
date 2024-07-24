package org.sopt.makers.internal.review.dto.response;

import java.util.List;

public record PagedActivityReviewResponse(

        List<ActivityReviewResponse> reviews,

        Boolean hasNext
) {
}
