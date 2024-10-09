package org.sopt.makers.internal.member.service.coffeechat;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.repository.coffeechat.CoffeeChatRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoffeeChatRetriever {

    private final CoffeeChatRepository coffeeChatRepository;

    public List<CoffeeChat> findCoffeeChatActivate(boolean isCoffeeChatActivate) {
        return coffeeChatRepository.findAllByIsCoffeeChatActivate(isCoffeeChatActivate);
    }
}
