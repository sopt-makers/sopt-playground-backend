package org.sopt.makers.internal.domain.Community;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SoptActivity {
    APPJAM("앱잼"),
    SOPKATHON("솝커톤"),
    SOPTERM("솝텀"),
    ;

    private final String value;

    public String getKey() {
        return name();
    }
}