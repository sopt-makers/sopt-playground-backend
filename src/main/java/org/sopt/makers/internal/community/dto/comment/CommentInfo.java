package org.sopt.makers.internal.community.dto.comment;

import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.dto.MemberVo;

public record CommentInfo(
        CommentDao commentDao,
        MemberVo memberVo,
        AnonymousProfile anonymousProfile
) {
}
