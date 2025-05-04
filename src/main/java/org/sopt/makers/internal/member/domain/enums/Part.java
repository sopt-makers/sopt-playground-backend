package org.sopt.makers.internal.member.domain.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Part {
    PLAN("기획"),
    DESIGN("디자인"),
    WEB("웹"),
    ANDROID("안드로이드"),
    IOS("iOS"),
    SERVER("서버"),
    ;

    private final String title;

    public String getKey() {
        return name();
    }
}
