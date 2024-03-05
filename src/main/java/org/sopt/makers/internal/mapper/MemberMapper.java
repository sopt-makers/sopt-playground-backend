package org.sopt.makers.internal.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.dto.internal.InternalMemberActivityResponse;
import org.sopt.makers.internal.dto.internal.InternalMemberProfileResponse;
import org.sopt.makers.internal.dto.internal.InternalMemberProfileSpecificResponse;
import org.sopt.makers.internal.dto.internal.InternalMemberResponse;
import org.sopt.makers.internal.dto.internal.InternalOfficialMemberResponse;
import org.sopt.makers.internal.dto.member.ActivityVo;
import org.sopt.makers.internal.dto.member.MakersMemberProfileResponse;
import org.sopt.makers.internal.dto.member.MemberProfileProjectDao;
import org.sopt.makers.internal.dto.member.MemberProfileProjectVo;
import org.sopt.makers.internal.dto.member.MemberProfileResponse;
import org.sopt.makers.internal.dto.member.MemberProfileSpecificResponse;
import org.sopt.makers.internal.dto.member.MemberProjectVo;
import org.sopt.makers.internal.dto.member.MemberResponse;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameGenerateResponse;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameRoomResponse;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberResponse toResponse(Member member);
    InternalMemberResponse toInternalResponse(Member member, Integer latestGeneration);
    MemberProfileResponse toProfileResponse (Member member);
    InternalMemberProfileResponse toInternalProfileResponse (Member member);
    WordChainGameGenerateResponse.UserResponse toUserResponse (Member member);
    WordChainGameRoomResponse.WordResponse.UserResponse toAllGameRoomResponse (Member member);
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
}
