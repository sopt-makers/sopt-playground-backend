package org.sopt.makers.internal.community.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;

public record AnonymousMentionRequest(

        @Schema(
                description = "익명 멘션된 사용자들의 익명 닉네임 배열",
                example = "[\"오너십있는 츄러스\", \"도전하는 빙수\"]"
        )
        String[] anonymousNicknames
) {
}
