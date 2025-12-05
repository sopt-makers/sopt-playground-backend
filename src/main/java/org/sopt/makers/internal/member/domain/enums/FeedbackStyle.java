package org.sopt.makers.internal.member.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackStyle {
    DIRECT("직설적"),
    INDIRECT("돌려서");

    @JsonValue
    private final String value;

    public static FeedbackStyle fromValue(String value) {
        if (value == null) return null;

        for (FeedbackStyle style : values()) {
            if (style.value.equals(value)) {
                return style;
            }
        }
        throw new IllegalArgumentException("Invalid feedback style: " + value);
    }
}