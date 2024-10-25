package org.sopt.makers.internal.member.domain.coffeechat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.internal.exception.ClientBadRequestException;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CoffeeChatSection {

    SOPT_ACTIVITY("SOPT 활동"),
    PLAN("기획"),
    DESIGN("디자인"),
    FRONTEND("프론트"),
    BACKEND("백엔드"),
    APP("앱 개발"),
    ETC("기타")
    ;

    private final String title;

    @JsonCreator
    public static CoffeeChatSection fromTitle(String title) {
        return Arrays.stream(CoffeeChatSection.values())
                .filter(section -> section.title.equals(title))
                .findFirst()
                .orElseThrow(() -> new ClientBadRequestException("Unknown CoffeeChat Section Title: " + title));
    }

    @JsonValue
    public String getTitle() {
        return title;
    }
}
