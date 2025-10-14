package org.sopt.makers.internal.community.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.aot.hint.annotation.Reflective;
import org.sopt.makers.internal.community.dto.AnonymousProfileVo;
import org.sopt.makers.internal.community.dto.CategoryVo;
import org.sopt.makers.internal.community.dto.CommunityPostVo;
import org.sopt.makers.internal.community.dto.MemberVo;

@Reflective
public record PostDetailResponse(
        MemberVo member,
        @Schema(required = true)
        CommunityPostVo posts,
        @Schema(required = true)
        CategoryVo category,
        @Schema(required = true)
        Boolean isMine,
        @Schema(required = true)
        Boolean isLiked,
        @Schema(required = true)
        Integer likes,
        @Schema(required = false)
        AnonymousProfileVo anonymousProfile
) {
    @QueryProjection
    public PostDetailResponse {}
}
