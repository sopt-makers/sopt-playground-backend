package org.sopt.makers.internal.member.service.coffeechat;

import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.springframework.stereotype.Component;

@Component
public class CoffeeChatUpdater {

    public void updateCoffeeChat(CoffeeChat coffeeChat, Boolean isCoffeeChatActivate, String coffeeChatBio) {

        coffeeChatBio = coffeeChatBio != null ? coffeeChatBio : "";
        coffeeChat.updateCoffeeChatInformation(isCoffeeChatActivate, coffeeChatBio);
    }

    public void updateCoffeeChatActivate(CoffeeChat coffeeChat, Boolean isCoffeeChatActivate) {
        coffeeChat.updateCoffeeChatActivate(isCoffeeChatActivate);
    }
}
