package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "질문 대상 멤버 응답 DTO")
public record
AskMemberResponse(
        @Schema(description = "질문 대상 멤버 목록")
        List<QuestionTargetMember> members
) {
    @Schema(description = "질문 대상 멤버 정보")
    public record QuestionTargetMember(
            @Schema(description = "멤버 ID")
            Long id,

            @Schema(description = "멤버 이름")
            String name,

            @Schema(description = "프로필 이미지 URL")
            String profileImage,

            @Schema(description = "소개")
            String introduction,

            @Schema(description = "최근 활동 정보")
            MemberSoptActivityResponse latestActivity,

            @Schema(description = "커리어 정보")
            MemberCareerResponse career,

            @Schema(description = "답변 보장 여부", example = "true")
            Boolean isAnswerGuaranteed
    ) {}

    public record MemberSoptActivityResponse(
            Integer generation,
            String part,
            String team
    ) {}

    public record MemberCareerResponse(
            String companyName,
            String title
    ) {}
}
