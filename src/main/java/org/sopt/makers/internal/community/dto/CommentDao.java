package org.sopt.makers.internal.community.dto;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;

import com.querydsl.core.annotations.QueryProjection;

public record CommentDao(
        Member member,
        CommunityComment comment
) {
    @QueryProjection
    public CommentDao {}
}
