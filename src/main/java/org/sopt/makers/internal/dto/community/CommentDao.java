package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;
import org.sopt.makers.internal.domain.community.CommunityComment;
import org.sopt.makers.internal.domain.Member;

public record CommentDao(
        Member member,
        CommunityComment comment
) {
    @QueryProjection
    public CommentDao {}
}
