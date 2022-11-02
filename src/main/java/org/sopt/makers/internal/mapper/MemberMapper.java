package org.sopt.makers.internal.mapper;

import org.mapstruct.Mapper;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.dto.member.MemberProfileResponse;
import org.sopt.makers.internal.dto.member.MemberProfileSpecificResponse;
import org.sopt.makers.internal.dto.member.MemberResponse;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberResponse toResponse(Member member);
    MemberProfileResponse toProfileResponse (Member member);
    MemberProfileSpecificResponse toProfileSpecificResponse (Member member, boolean isMine);
}
