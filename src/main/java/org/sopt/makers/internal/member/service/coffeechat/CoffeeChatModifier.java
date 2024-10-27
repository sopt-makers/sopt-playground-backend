package org.sopt.makers.internal.member.service.coffeechat;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.controller.coffeechat.dto.request.CoffeeChatDetailsRequest;
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

    // UPDATE

    public void updateCoffeeChat(CoffeeChat coffeeChat, Boolean isCoffeeChatActivate, String coffeeChatBio) {

        coffeeChatBio = coffeeChatBio != null ? coffeeChatBio : "";
        coffeeChat.updateCoffeeChatInformation(isCoffeeChatActivate, coffeeChatBio);
    }

    public void updateCoffeeChatActivate(CoffeeChat coffeeChat, Boolean isCoffeeChatActivate) {
        coffeeChat.updateCoffeeChatActivate(isCoffeeChatActivate);
    }


    // DELETE

    public void deleteCoffeeChatDetails(CoffeeChat coffeeChat) {

        coffeeChatRepository.delete(coffeeChat);
    }
}
