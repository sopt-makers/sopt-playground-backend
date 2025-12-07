package org.sopt.makers.internal.member.mapper;

import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.WorkPreference;
import org.sopt.makers.internal.member.dto.ActivityVo;
import org.sopt.makers.internal.member.dto.MemberProfileProjectDao;
import org.sopt.makers.internal.member.dto.MemberProfileProjectVo;
import org.sopt.makers.internal.member.dto.MemberProjectVo;
import org.sopt.makers.internal.member.dto.response.MemberProfileResponse;
import org.sopt.makers.internal.member.dto.response.MemberProfileSpecificResponse;
import org.sopt.makers.internal.member.dto.response.WorkPreferenceRecommendationResponse;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    @Mapping(target = "name", source = "userDetails.name")
    @Mapping(target = "profileImage", source = "userDetails.profileImage")
    @Mapping(target = "birthday", source = "userDetails.birthday")
    @Mapping(target = "phone", source = "userDetails.phone")
    @Mapping(target = "email", source = "userDetails.email")
    @Mapping(target = "activities", source = "userDetails.soptActivities")
    MemberProfileResponse toProfileResponse (Member member, InternalUserDetails userDetails, Boolean isCoffeeChatActivate);

    @Mapping(target = "projects", source = "projects")
    MemberProfileProjectVo toSoptMemberProfileProjectVo(SoptActivity member, List<MemberProjectVo> projects);

    @Mapping(target = "name", source = "userDetails.name")
    @Mapping(target = "profileImage", source = "userDetails.profileImage")
    @Mapping(target = "birthday", expression = "java(userDetails.birthday() != null ? java.time.LocalDate.parse(userDetails.birthday()) : null)")
    @Mapping(target = "phone", source = "userDetails.phone")
    @Mapping(target = "email", source = "userDetails.email")
    @Mapping(target = "activities", source = "activities")
    @Mapping(target = "workPreference", expression = "java(toWorkPreferenceResponse(member.getWorkPreference()))")
    MemberProfileSpecificResponse toProfileSpecificResponse (
            Member member,
            InternalUserDetails userDetails,
            boolean isMine,
            List<MemberProfileProjectDao> projects,
            List<MemberProfileSpecificResponse.MemberActivityResponse> activities,
            Boolean isCoffeeChatActivate
    );

    @Mapping(target="id", source="activity.activityId")
    ActivityVo toActivityInfoVo (SoptActivity activity, boolean isProject);

    @Mapping(source = "project.name", target = "team")
    ActivityVo toActivityInfoVo (MemberProfileProjectDao project, boolean isProject, String part);

    MemberProjectVo toActivityInfoVo (MemberProfileProjectDao project);

    default MemberProfileSpecificResponse.WorkPreferenceResponse toWorkPreferenceResponse(WorkPreference workPreference) {
        if (workPreference == null) {
            return null;
        }
        return new MemberProfileSpecificResponse.WorkPreferenceResponse(
            workPreference.getIdeationStyleValue(),
            workPreference.getWorkTimeValue(),
            workPreference.getCommunicationStyleValue(),
            workPreference.getWorkPlaceValue(),
            workPreference.getFeedbackStyleValue()
        );
    }

    default WorkPreferenceRecommendationResponse.WorkPreferenceData mapWorkPreferenceData(
            WorkPreference workPreference) {

        if (workPreference == null) {
            return null;
        }

        return new WorkPreferenceRecommendationResponse.WorkPreferenceData(
                workPreference.getIdeationStyleValue(),
                workPreference.getWorkTimeValue(),
                workPreference.getCommunicationStyleValue(),
                workPreference.getWorkPlaceValue(),
                workPreference.getFeedbackStyleValue()
        );
    }

    default List<WorkPreferenceRecommendationResponse.RecommendedMember> toRecommendedMembersWithMatchPercentage(
            List<Member> members,
            Map<Long, InternalUserDetails> userDetailsMap,
            WorkPreference currentUserPreference,
            org.sopt.makers.internal.member.service.workpreference.WorkPreferenceRetriever workPreferenceRetriever) {

        return members.stream()
                .map(member -> {
                    InternalUserDetails userDetails = userDetailsMap.get(member.getId());
                    int matchCount = workPreferenceRetriever.calculateMatchCount(currentUserPreference, member.getWorkPreference());
                    int matchPercentage = calculateMatchPercentage(matchCount);

                    return toRecommendedMemberWithMatchPercentage(member, userDetails, matchPercentage);
                })
                .toList();
    }

    default WorkPreferenceRecommendationResponse.RecommendedMember toRecommendedMemberWithMatchPercentage(
            Member member, InternalUserDetails userDetails, int matchPercentage) {

        WorkPreferenceRecommendationResponse.MemberSoptActivityResponse currentGenerationActivity =
                userDetails.soptActivities().stream()
                        .filter(activity -> activity.generation() == org.sopt.makers.internal.common.Constant.CURRENT_GENERATION)
                        .findFirst()
                        .map(activity -> new WorkPreferenceRecommendationResponse.MemberSoptActivityResponse(
                                (long) activity.activityId(),
                                activity.generation(),
                                activity.part(),
                                activity.team()
                        ))
                        .orElse(null);

        WorkPreferenceRecommendationResponse.WorkPreferenceData workPreferenceData =
                mapWorkPreferenceData(member.getWorkPreference());

        return new WorkPreferenceRecommendationResponse.RecommendedMember(
                member.getId(),
                userDetails.name(),
                userDetails.profileImage(),
                member.getUniversity(),
                currentGenerationActivity,
                matchPercentage,
                workPreferenceData
        );
    }

    default int calculateMatchPercentage(int matchCount) {
        return switch (matchCount) {
            case 5 -> 100;
            case 4 -> 80;
            case 3 -> 60;
            default -> 0;
        };
    }
}
