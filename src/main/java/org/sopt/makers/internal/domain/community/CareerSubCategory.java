package org.sopt.makers.internal.domain.community;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)

public enum CareerSubCategory {
    POSTSCRIPT("후기"),
    TIPS("꿀팁"),
    ;

    private final String value;

    public String getKey() {
        return name();
    }
}