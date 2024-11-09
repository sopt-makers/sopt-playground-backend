package org.sopt.makers.internal.member.mapper;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.member.controller.dto.response.MemberInfoResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberResponseMapper {

    public MemberInfoResponse toMemberInfoResponse(Member member, Boolean isCoffeeChatActive) {

        return new MemberInfoResponse(
                member.getId(),
                member.getName(),
                member.getGeneration(),
                member.getProfileImage(),
                member.getHasProfile(),
                member.getEditActivitiesAble(),
                isCoffeeChatActive
        );
    }
}
