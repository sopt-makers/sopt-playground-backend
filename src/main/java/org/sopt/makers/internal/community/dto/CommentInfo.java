package org.sopt.makers.internal.community.dto;

import org.sopt.makers.internal.community.domain.anonymous.AnonymousCommentProfile;

public record CommentInfo(
        CommentDao commentDao,
        MemberVo memberVo,
        AnonymousCommentProfile anonymousCommentProfile
) {
}
