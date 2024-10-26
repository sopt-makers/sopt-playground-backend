package org.sopt.makers.internal.member.service.coffeechat;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.member.domain.coffeechat.Career;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatSection;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;
import org.sopt.makers.internal.member.repository.coffeechat.CoffeeChatRepository;
import org.sopt.makers.internal.member.repository.coffeechat.dto.CoffeeChatInfoDto;
import org.sopt.makers.internal.member.repository.coffeechat.dto.RecentCoffeeChatInfoDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoffeeChatRetriever {

    private final CoffeeChatRepository coffeeChatRepository;

    public List<CoffeeChat> findCoffeeChatActivate(boolean isCoffeeChatActivate) {
        return coffeeChatRepository.findAllByIsCoffeeChatActivate(isCoffeeChatActivate);
    }

    public CoffeeChat findCoffeeChatByMember(Member member) {
        return coffeeChatRepository.findCoffeeChatByMember(member)
                .orElseThrow(() -> new NotFoundDBEntityException("커피챗 정보를 등록한적 없는 유저입니다. " + "member id: " + member.getId()));
    }

    public void checkAlreadyExistCoffeeChat(Member member) {
        if (coffeeChatRepository.existsCoffeeChatByMember(member)) {
            throw new ClientBadRequestException("이미 커피챗 정보가 등록된 유저입니다. " + "member id: " + member.getId());
        }
    }

    public boolean existsCoffeeChat(Member member) {
        return coffeeChatRepository.existsCoffeeChatByMember(member);
    }

    public List<RecentCoffeeChatInfoDto> recentCoffeeChatInfoList() {
        return coffeeChatRepository.findRecentCoffeeChatInfo();
    }

    public List<CoffeeChatInfoDto> searchCoffeeChatInfo(Long memberId, CoffeeChatSection section, CoffeeChatTopicType topicType, Career career, String part, String search) {
        return coffeeChatRepository.findSearchCoffeeChatInfo(memberId, section, topicType, career, part, search);
    }
}