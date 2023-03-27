package org.sopt.makers.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.dto.internal.InternalMemberProfileResponse;
import org.sopt.makers.internal.dto.internal.InternalMemberProfileSpecificResponse;
import org.sopt.makers.internal.dto.internal.InternalMemberResponse;
import org.sopt.makers.internal.dto.member.*;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberResponse toResponse(Member member);
    InternalMemberResponse toInternalResponse(Member member);
    MemberProfileResponse toProfileResponse (Member member);
    InternalMemberProfileResponse toInternalProfileResponse (Member member);

    MakersMemberProfileResponse toMakersMemberProfileResponse (Member member);

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
}
