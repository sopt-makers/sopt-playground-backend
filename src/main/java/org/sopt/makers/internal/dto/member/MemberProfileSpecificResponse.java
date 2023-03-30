package org.sopt.makers.internal.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import lombok.val;

public record MemberProfileSpecificResponse(
        @Schema(required = true)
        String name,
        String profileImage,
        LocalDate birthday,
        String phone,
        String email,
        String address,
        String university,
        String major,
        String introduction,
        String skill,
        String mbti,
        String mbtiDescription,
        Double sojuCapacity,
        String interest,
        UserFavorResponse userFavor,
        String idealType,
        String selfIntroduction,
        @Schema(required = true)
        List<MemberActivityResponse> activities,
        @Schema(required = true)
        List<SoptMemberActivityResponse> soptActivities,
        List<MemberLinkResponse> links,
        List<MemberProjectResponse> projects,
        List<MemberCareerResponse> careers,
        Boolean allowOfficial,
        @Schema(required = true)
        Boolean isMine
) {
    public record MemberLinkResponse(
            Long id,
            String title,
            String url
    ){}

    public record UserFavorResponse(
            Boolean isPourSauceLover,
            Boolean isHardPeachLover,
            Boolean isMintChocoLover,
            Boolean isRedBeanFishBreadLover,
            Boolean isSojuLover,
            Boolean isRiceTteokLover
    ){}

    public record MemberProjectResponse(
            Long id,
            String name,
            String summary,
            Integer generation,
            String category,
            String logoImage,
            String thumbnailImage,
            String[] serviceType
    ){}

    public record MemberActivityResponse (
            String cardinalInfo,
            List<ActivityVo> cardinalActivities
    ){}

    public record SoptMemberActivityResponse (
            Integer generation,
            String part,
            String team,
            List<MemberProjectVo> projects
    ) {

        public SoptMemberActivityResponse (Integer generation, String part, String team, List<MemberProjectVo> projects) {
            this.generation = generation;
            this.part = part;
            val teamNullCondition = (team == null || team.equals("해당 없음"));
            if (teamNullCondition) {
                team = null;
            }
            this.team = team;
            this.projects = projects;
        }
    }

    public record MemberCareerResponse(
            Long id,
            String companyName,
            String title,
            String startDate,
            String endDate,
            Boolean isCurrent
    ){}
}
