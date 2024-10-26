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
public class CoffeeChatModifier {

    private final CoffeeChatRepository coffeeChatRepository;
    private final CoffeeChatHistoryRepository coffeeChatHistoryRepository;

    // CREATE

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

    // UPDATE

    public void updateCoffeeChatActivate(CoffeeChat coffeeChat, Boolean isCoffeeChatActivate) {
        coffeeChat.updateCoffeeChatActivate(isCoffeeChatActivate);
    }


    // DELETE

    public void deleteCoffeeChatDetails(CoffeeChat coffeeChat) {

        coffeeChatRepository.delete(coffeeChat);
    }
}
