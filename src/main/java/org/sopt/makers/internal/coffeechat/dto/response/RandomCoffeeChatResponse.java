package org.sopt.makers.internal.coffeechat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record RandomCoffeeChatResponse(
        @Schema(description = "멤버 ID")
        Long memberId,

        @Schema(description = "커피챗 제목")
        String coffeeChatBio,

        @Schema(description = "프로필 이미지 URL")
        String profileImage,

        @Schema(description = "이름")
        String name,

        @Schema(description = "경력 차수 (예: 주니어 (0-3년))")
        String career,

        @Schema(description = "회사명 (없으면 학교명)")
        String organization,

        @Schema(description = "직무")
        String companyJob,

        @Schema(description = "솝트 활동 정보 (예: [\"35기 서버\", \"3기 메이커스\"])")
        List<String> soptActivities,

        @Schema(description = "커피챗 주제 타입 목록 (예: [\"커리어\", \"네트워킹\"])")
        List<String> topicTypeList
) {}
