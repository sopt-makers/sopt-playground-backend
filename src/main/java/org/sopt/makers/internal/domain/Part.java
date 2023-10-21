package org.sopt.makers.internal.domain;

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

    private final String value;

    public String getKey() {
        return name();
    }
}