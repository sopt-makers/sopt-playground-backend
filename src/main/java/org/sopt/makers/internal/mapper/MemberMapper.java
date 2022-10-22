package org.sopt.makers.internal.mapper;

import org.mapstruct.Mapper;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.dto.member.MemberResponse;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberResponse toResponse(Member member);
}
