package org.sopt.makers.internal.member.mapper;

import java.util.List;
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
            workPreference.getIdeationStyle(),
            workPreference.getWorkTime(),
            workPreference.getCommunicationStyle(),
            workPreference.getWorkPlace(),
            workPreference.getFeedbackStyle()
        );
    }
}
