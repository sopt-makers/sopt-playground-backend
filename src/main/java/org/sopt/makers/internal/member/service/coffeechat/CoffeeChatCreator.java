package org.sopt.makers.internal.member.service.coffeechat;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.dto.request.CoffeeChatDetailsRequest;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatHistory;
import org.sopt.makers.internal.member.repository.coffeechat.CoffeeChatHistoryRepository;
import org.sopt.makers.internal.member.repository.coffeechat.CoffeeChatRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoffeeChatCreator {  // TODO Creator 대신 C,U,D를 수행하는 컴포넌트를 공통으로 만들면 어떨지? (파일 수가 많아지는 것 대비)

    private final CoffeeChatRepository coffeeChatRepository;
    private final CoffeeChatHistoryRepository coffeeChatHistoryRepository;

    // TODO deprecate 예정
    public void createCoffeeChat(Member member, String coffeeChatBio) {

        coffeeChatRepository.save(CoffeeChat.builder()
                .member(member)
                .coffeeChatBio(coffeeChatBio)
                .build()
        );
    }

    public void createCoffeeChatDetails(Member member, CoffeeChatDetailsRequest request) {

        coffeeChatRepository.save(CoffeeChat.builder()
                .member(member)
                .career(request.memberInfo().career())
                .introduction(request.memberInfo().introduction())
                .section(request.coffeeChatInfo().sections())
                .coffeeChatBio(request.coffeeChatInfo().bio())
                .coffeeChatTopicType(request.coffeeChatInfo().topicTypes())
                .topic(request.coffeeChatInfo().topic())
                .meetingType(request.coffeeChatInfo().meetingType())
                .guideline(request.coffeeChatInfo().guideline()).build()
        );
    }

    public void createCoffeeChatHistory(Member sender, Member receiver, String content) {

        coffeeChatHistoryRepository.save(CoffeeChatHistory.builder()
                .sender(sender)
                .receiver(receiver)
                .requestContent(content)
                .build());
    }

    public void deleteCoffeeChatDetails(CoffeeChat coffeeChat) {

        coffeeChatRepository.delete(coffeeChat);
    }
}
