package org.sopt.makers.internal.member.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IdeationStyle {
    SPONTANEOUS("즉흥"),
    DELIBERATE("숙고");

    @JsonValue
    private final String value;

    public static IdeationStyle fromValue(String value) {
        if (value == null) return null;

        for (IdeationStyle style : values()) {
            if (style.value.equals(value)) {
                return style;
            }
        }
        throw new IllegalArgumentException("Invalid ideation style: " + value);
    }
}