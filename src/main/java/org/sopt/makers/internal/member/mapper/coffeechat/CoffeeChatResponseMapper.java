package org.sopt.makers.internal.member.mapper.coffeechat;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.member.controller.coffeechat.dto.response.RecentCoffeeChatResponse.RecentCoffeeChat;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;
import org.sopt.makers.internal.member.dto.response.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.member.repository.coffeechat.dto.CoffeeChatInfoDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CoffeeChatResponseMapper {

    public List<CoffeeChatVo> toCoffeeChatResponse(List<CoffeeChat> coffeeChatList, List<Member> memberList, List<MemberCareer> memberCareerList) {
        return IntStream.range(0, coffeeChatList.size())
                .mapToObj(index -> new CoffeeChatVo(
                        memberList.get(index).getId(),
                        memberList.get(index).getName(),
                        memberList.get(index).getProfileImage(),
                        Optional.ofNullable(memberCareerList.get(index))
                                .map(MemberCareer::getCompanyName)
                                .orElse(memberList.get(index).getUniversity()),
                        Optional.ofNullable(memberCareerList.get(index))
                                .map(MemberCareer::getTitle)
                                .orElse(memberList.get(index).getSkill()),
                        coffeeChatList.get(index).getCoffeeChatBio(),
                        coffeeChatList.get(index).getCoffeeChatTopicType(),
                        coffeeChatList.get(index).getSection()
                ))
                .collect(Collectors.toList());
    }

    public RecentCoffeeChat toRecentCoffeeChatResponse(CoffeeChatInfoDto coffeeChatInfo, MemberCareer memberCareer, List<String> soptActivities) {
        return new RecentCoffeeChat(
                coffeeChatInfo.memberId(),
                coffeeChatInfo.bio(),
                coffeeChatInfo.topicTypeList().stream().map(CoffeeChatTopicType::getTitle).toList(),
                coffeeChatInfo.profileImage(),
                coffeeChatInfo.name(),
                coffeeChatInfo.career().getTitle(),
                memberCareer != null ? memberCareer.getCompanyName() : coffeeChatInfo.university(),
                memberCareer != null ? memberCareer.getTitle() : null,
                soptActivities
        );
    }
}