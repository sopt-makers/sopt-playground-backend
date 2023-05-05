package org.sopt.makers.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.dto.internal.*;
import org.sopt.makers.internal.dto.member.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberResponse toResponse(Member member);
    InternalMemberResponse toInternalResponse(Member member);
    MemberProfileResponse toProfileResponse (Member member);
    InternalMemberProfileResponse toInternalProfileResponse (Member member);

    MakersMemberProfileResponse toMakersMemberProfileResponse (Member member);

    @Mapping(target = "projects", source = "projects")
    MemberProfileProjectVo toSoptMemberProfileProjectVo(MemberSoptActivity member, List<MemberProjectVo> projects);

    @Mapping(target = "activities", source = "activities")
    MemberProfileSpecificResponse toProfileSpecificResponse (
            Member member,
            boolean isMine,
            List<MemberProfileProjectDao> projects,
            List<MemberProfileSpecificResponse.MemberActivityResponse> activities,
            List<MemberProfileProjectVo> soptActivities
    );

    @Mapping(target = "activities", source = "activities")
    MemberProfileSpecificResponse toProfileSpecificResponse (
            Member member,
            boolean isMine,
            List<MemberProfileProjectDao> projects,
            List<MemberProfileSpecificResponse.MemberActivityResponse> activities
    );

    @Mapping(target = "activities", source = "activities")
    InternalMemberProfileSpecificResponse toInternalProfileSpecificResponse (
            Member member,
            boolean isMine,
            List<MemberProfileProjectDao> projects,
            List<InternalMemberProfileSpecificResponse.MemberActivityResponse> activities
    );

    ActivityVo toActivityInfoVo (MemberSoptActivity activity, boolean isProject);

    @Mapping(source = "project.name", target = "team")
    ActivityVo toActivityInfoVo (MemberProfileProjectDao project, boolean isProject, String part);

    MemberProjectVo toActivityInfoVo (MemberProfileProjectDao project);
    InternalOfficialMemberResponse toOfficialResponse(Member member, String part, Integer generation);
}
