package org.sopt.makers.internal.community.dto.comment;

import org.springframework.aot.hint.annotation.Reflective;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;

@Reflective
public record CommentDao(
        Member member,
        CommunityComment comment
) {
}
