package org.sopt.makers.internal.member.domain.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ServiceType {
    WEB("웹"),
    APP("앱"),
    ;

    private final String title;

    public String getKey() {
        return name();
    }
}
