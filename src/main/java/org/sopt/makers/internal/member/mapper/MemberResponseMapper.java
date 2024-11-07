package org.sopt.makers.internal.member.mapper;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.member.controller.dto.response.MemberInfoResponse;
import org.sopt.makers.internal.member.controller.dto.response.MemberPropertiesResponse;
import org.sopt.makers.internal.member.service.coffeechat.dto.MemberCoffeeChatPropertyDto;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public MemberPropertiesResponse toMemberPropertiesResponse(
            Member member,
            MemberCareer memberCareer,
            MemberCoffeeChatPropertyDto coffeeChatProperty,
            List<String> activitiesAndGeneration
    ) {
        List<Integer> generations = activitiesAndGeneration.stream()
                .map(item -> Integer.parseInt(item.split(" ")[0].replaceAll("[^0-9]", ""))
                )
                .toList();

        List<String> activities = activitiesAndGeneration.stream()
                .map(item -> item.split(" ")[1])
                .toList();

        return new MemberPropertiesResponse(
                member.getId(),
                memberCareer == null ? member.getMajor() : null,
                memberCareer != null ? memberCareer.getTitle() : null,
                memberCareer != null ? memberCareer.getCompanyName() : member.getUniversity(),
                activities,
                generations,
                coffeeChatProperty.coffeeChatStatus(),
                coffeeChatProperty.receivedCoffeeChatCount(),
                coffeeChatProperty.sentCoffeeChatCount()
        );
    }
}
