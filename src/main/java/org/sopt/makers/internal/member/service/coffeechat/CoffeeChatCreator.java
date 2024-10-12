package org.sopt.makers.internal.member.service.coffeechat;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.repository.coffeechat.CoffeeChatRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoffeeChatCreator {

    private final CoffeeChatRepository coffeeChatRepository;

    public void createCoffeeChat(Member member, String coffeeChatBio) {

        coffeeChatRepository.save(CoffeeChat.builder()
                .member(member)
                .coffeeChatBio(coffeeChatBio)
                .build()
        );
    }
}
