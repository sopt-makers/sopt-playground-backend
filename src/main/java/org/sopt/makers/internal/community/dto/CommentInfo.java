package org.sopt.makers.internal.community.dto;

import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;

public record CommentInfo(
        CommentDao commentDao,
        MemberVo memberVo,
        AnonymousProfile anonymousProfile
) {
}
