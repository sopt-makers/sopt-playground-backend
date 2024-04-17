package org.sopt.makers.internal.dto.community;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.community.CommunityComment;

import com.querydsl.core.annotations.QueryProjection;

public record CommentDao(
        Member member,
        CommunityComment comment
) {
    @QueryProjection
    public CommentDao {}
}
