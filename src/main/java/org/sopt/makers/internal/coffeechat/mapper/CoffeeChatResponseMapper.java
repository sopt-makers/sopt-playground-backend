package org.sopt.makers.internal.coffeechat.mapper;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.MemberSimpleResonse;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.exception.BusinessLogicException;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatDetailResponse;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.coffeechat.dto.response.InternalCoffeeChatMemberResponse;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChat;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatSection;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatTopicType;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatInfoDto;
import org.sopt.makers.internal.coffeechat.dto.request.InternalCoffeeChatMemberDto;
import org.sopt.makers.internal.coffeechat.dto.request.RecentCoffeeChatInfoDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoffeeChatResponseMapper {

    public CoffeeChatDetailResponse toCoffeeChatDetailResponse(CoffeeChat coffeeChat, Member member, InternalUserDetails userDetails, MemberCareer memberCareer, Boolean isMine) {

        if (!coffeeChat.getMember().getId().equals(member.getId())) {
            throw new BusinessLogicException("잘못된 커피챗 사용자가 전달되었습니다.");
        }

        return new CoffeeChatDetailResponse(
                coffeeChat.getCoffeeChatBio(),
                member.getId(),
                userDetails.profileImage(),
                userDetails.name(),
                coffeeChat.getCareer().getTitle(),
                memberCareer != null ? memberCareer.getCompanyName() : member.getUniversity(),
                memberCareer != null ? memberCareer.getTitle() : null,
                member.getIsPhoneBlind() ? null : userDetails.phone(),
                userDetails.email(),
                coffeeChat.getIntroduction(),
                coffeeChat.getSection().stream().map(CoffeeChatSection::getTitle).toList(),
                coffeeChat.getCoffeeChatTopicType().stream().map(CoffeeChatTopicType::getTitle).toList(),
                coffeeChat.getTopic(),
                coffeeChat.getMeetingType().getTitle(),
                coffeeChat.getGuideline(),
                coffeeChat.getIsCoffeeChatActivate(),
                isMine,
                !coffeeChat.getIsCoffeeChatActivate()
        );
    }

    public CoffeeChatVo toRecentCoffeeChatResponse(RecentCoffeeChatInfoDto coffeeChatInfo, MemberCareer memberCareer, List<String> soptActivities, MemberSimpleResonse memberSimpleResonse) {
        return new CoffeeChatVo(
                coffeeChatInfo.memberId(),
                coffeeChatInfo.bio(),
                coffeeChatInfo.topicTypeList().stream().map(CoffeeChatTopicType::getTitle).toList(),
                memberSimpleResonse.profileImage(),
                memberSimpleResonse.name(),
                coffeeChatInfo.career().getTitle(),
                memberCareer != null ? memberCareer.getCompanyName() : coffeeChatInfo.university(),
                memberCareer != null ? memberCareer.getTitle() : null,
                soptActivities,
                null,
                null
        );
    }

    public CoffeeChatVo toCoffeeChatResponse(CoffeeChatInfoDto coffeeChatInfo, MemberCareer memberCareer, List<String> soptActivities, MemberSimpleResonse memberSimpleResonse) {
        return new CoffeeChatVo(
                coffeeChatInfo.memberId(),
                coffeeChatInfo.bio(),
                coffeeChatInfo.topicTypeList().stream().map(CoffeeChatTopicType::getTitle).toList(),
                memberSimpleResonse.profileImage(),
                memberSimpleResonse.name(),
                coffeeChatInfo.career().getTitle(),
                memberCareer != null ? memberCareer.getCompanyName() : coffeeChatInfo.university(),
                memberCareer != null ? memberCareer.getTitle() : null,
                soptActivities,
                coffeeChatInfo.isMine(),
                coffeeChatInfo.isBlind()
        );
    }

    public List<InternalCoffeeChatMemberResponse> toInternalCoffeeChatMemberResponse(List<InternalCoffeeChatMemberDto> coffeeChatMembers) {
        return coffeeChatMembers.stream().map(
                member -> new InternalCoffeeChatMemberResponse(
                    member.id(), member.parts(), member.name(), member.profileImage()
                )
        ).toList();
    }
}