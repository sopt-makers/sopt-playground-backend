package org.sopt.makers.internal.member.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sopt.makers.internal.internal.dto.InternalMemberActivityResponse;
import org.sopt.makers.internal.internal.dto.InternalMemberProfileResponse;
import org.sopt.makers.internal.internal.dto.InternalMemberProfileSpecificResponse;
import org.sopt.makers.internal.internal.dto.InternalMemberResponse;
import org.sopt.makers.internal.internal.dto.InternalOfficialMemberResponse;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberSoptActivity;
import org.sopt.makers.internal.member.dto.ActivityVo;
import org.sopt.makers.internal.member.dto.MemberProfileProjectDao;
import org.sopt.makers.internal.member.dto.MemberProfileProjectVo;
import org.sopt.makers.internal.member.dto.MemberProjectVo;
import org.sopt.makers.internal.member.dto.response.MemberProfileResponse;
import org.sopt.makers.internal.member.dto.response.MemberProfileSpecificResponse;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    InternalMemberResponse toInternalResponse(Member member, Integer latestGeneration);
    MemberProfileResponse toProfileResponse (Member member, Boolean isCoffeeChatActivate);
    InternalMemberProfileResponse toInternalProfileResponse (Member member);

    @Mapping(target = "projects", source = "projects")
    MemberProfileProjectVo toSoptMemberProfileProjectVo(MemberSoptActivity member, List<MemberProjectVo> projects);

    @Mapping(target = "activities", source = "activities")
    MemberProfileSpecificResponse toProfileSpecificResponse (
        Member member,
        boolean isMine,
        List<MemberProfileProjectDao> projects,
        List<MemberProfileSpecificResponse.MemberActivityResponse> activities,
        List<MemberProfileProjectVo> soptActivities,
        Boolean isCoffeeChatActivate
    );

    @Mapping(target = "activities", source = "activities")
    InternalMemberProfileSpecificResponse toInternalProfileSpecificResponse (
            Member member,
            boolean isMine,
            List<MemberProfileProjectDao> projects,
            List<InternalMemberProfileSpecificResponse.MemberActivityResponse> activities
    );

    @Mapping(target = "activities", source = "activities")
    InternalMemberActivityResponse toInternalMemberActivityResponse (
            Member member,
            List<InternalMemberActivityResponse.MemberSoptActivityResponse> activities
    );

    ActivityVo toActivityInfoVo (MemberSoptActivity activity, boolean isProject);

    @Mapping(source = "project.name", target = "team")
    ActivityVo toActivityInfoVo (MemberProfileProjectDao project, boolean isProject, String part);

    MemberProjectVo toActivityInfoVo (MemberProfileProjectDao project);

    @Mapping(source = "generation", target = "generation")
    InternalOfficialMemberResponse toOfficialResponse(Member member, String part, Integer generation);

    default String mapPhoneIfBlind(Boolean isPhoneBlind, String phone) {
        return isPhoneBlind ? null : phone;
    }
}
