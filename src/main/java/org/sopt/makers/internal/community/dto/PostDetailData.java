package org.sopt.makers.internal.community.dto;

import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.vote.dto.response.VoteResponse;

public record PostDetailData(
        CommunityPost post,
        InternalUserDetails userDetails,
        MemberCareer authorCareer,
        Category category,
        VoteResponse vote
) {
}
