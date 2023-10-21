package org.sopt.makers.internal.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Promotion {
    RECRUIT("모집"),
    DESIGN("디자인"),
    PROJECT("프로젝트"),
    EVENT("행사"),
    ;

    private final String value;

    public String getKey() {
        return name();
    }
}
