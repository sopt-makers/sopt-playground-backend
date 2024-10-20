package org.sopt.makers.internal.member.mapper.coffeechat;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.exception.BusinessLogicException;
import org.sopt.makers.internal.member.controller.coffeechat.dto.response.CoffeeChatDetailResponse;
import org.sopt.makers.internal.member.controller.coffeechat.dto.response.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;
import org.sopt.makers.internal.member.repository.coffeechat.dto.CoffeeChatInfoDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoffeeChatResponseMapper {

//    public List<CoffeeChatVo> toCoffeeChatResponse(List<CoffeeChat> coffeeChatList, List<Member> memberList, List<MemberCareer> memberCareerList) {
//        return IntStream.range(0, coffeeChatList.size())
//                .mapToObj(index -> new CoffeeChatVo(
//                        memberList.get(index).getId(),
//                        memberList.get(index).getName(),
//                        memberList.get(index).getProfileImage(),
//                        Optional.ofNullable(memberCareerList.get(index))
//                                .map(MemberCareer::getCompanyName)
//                                .orElse(memberList.get(index).getUniversity()),
//                        Optional.ofNullable(memberCareerList.get(index))
//                                .map(MemberCareer::getTitle)
//                                .orElse(memberList.get(index).getSkill()),
//                        coffeeChatList.get(index).getCoffeeChatBio(),
//                        coffeeChatList.get(index).getCoffeeChatTopicType(),
//                        coffeeChatList.get(index).getSection()
//                ))
//                .collect(Collectors.toList());
//    }

    public CoffeeChatDetailResponse toCoffeeChatDetailResponse(CoffeeChat coffeeChat, Member member, MemberCareer memberCareer, Boolean isMine) {

        if (!coffeeChat.getMember().getId().equals(member.getId())) {
            throw new BusinessLogicException("잘못된 커피챗 사용자가 전달되었습니다.");
        }

        return new CoffeeChatDetailResponse(
                coffeeChat.getCoffeeChatBio(),
                member.getId(),
                member.getName(),
                coffeeChat.getCareer().getTitle(),
                memberCareer != null ? memberCareer.getCompanyName() : member.getUniversity(),
                memberCareer != null ? memberCareer.getTitle() : null,
                member.getIsPhoneBlind() ? null : member.getPhone(),
                member.getEmail(),
                coffeeChat.getIntroduction(),
                coffeeChat.getCoffeeChatTopicType().stream().map(CoffeeChatTopicType::getTitle).toList(),
                coffeeChat.getTopic(),
                coffeeChat.getMeetingType().getTitle(),
                coffeeChat.getGuideline(),
                coffeeChat.getIsCoffeeChatActivate(),
                isMine
        );
    }

    public CoffeeChatVo toRecentCoffeeChatResponse(CoffeeChatInfoDto coffeeChatInfo, MemberCareer memberCareer, List<String> soptActivities) {
        return new CoffeeChatVo(
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