package org.sopt.makers.internal.coffeechat.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CoffeeChatStatus {

    ON(true),
    OFF(false),
    NONE(false)
    ;

    private final boolean isCoffeeChatActivate;
}
