package org.sopt.makers.internal.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryGroup;
import org.sopt.makers.internal.community.domain.enums.CommunityPostSourceType;
import org.sopt.makers.internal.community.domain.enums.CommunityPostTag;
import org.sopt.makers.internal.community.dto.AnonymousProfileVo;
import org.sopt.makers.internal.community.dto.MemberVo;
import org.sopt.makers.internal.vote.dto.response.VoteResponse;

import java.util.List;

public record PostResponse(
        @Schema(required = true)
        Long id,
        CommunityPostSourceType sourceType,
        MemberVo member,
        Long writerId,
        Boolean isMine,
        Boolean isLiked,
        Integer likes,
        CommunityCategoryGroup categoryGroup,
        CommunityCategoryCode categoryCode,
        String categoryName,
        List<CommunityPostTag> tags,
        String title,
        String content,
        Integer hits,
        Integer commentCount,
        List<String> images,
        Boolean isBlindWriter,
        String sopticleUrl,
        AnonymousProfileVo anonymousProfile,
        String createdAt,
        List<CommentResponse> comments,
        VoteResponse vote,
        Long meetingId //크루팀 모임Id
) {}
