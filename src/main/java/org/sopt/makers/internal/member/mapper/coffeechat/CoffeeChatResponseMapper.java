package org.sopt.makers.internal.member.mapper.coffeechat;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.dto.member.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoffeeChatResponseMapper {

    private final MemberRetriever memberRetriever;

    private final MemberCareerRetriever memberCareerRetriever;

    public List<CoffeeChatVo> toCoffeeChatResponse(List<CoffeeChat> coffeeChatList) {

        return coffeeChatList.stream().map(coffeeChat -> {
            Member member = memberRetriever.findMemberById(coffeeChat.getMember().getId());
            MemberCareer career = memberCareerRetriever.findMemberLastCareerByMemberId(member.getId());
            return new CoffeeChatVo(
                    member.getId(),
                    member.getName(),
                    member.getProfileImage(),
                    career != null ? career.getCompanyName() : member.getUniversity(),
                    career != null ? career.getTitle() : member.getSkill(),
                    coffeeChat.getCoffeeChatBio()
            );
        }).toList();
    }
}
