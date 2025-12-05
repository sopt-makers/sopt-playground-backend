package org.sopt.makers.internal.member.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunicationStyle {
    BATCH("몰아서"),
    DISTRIBUTED("나눠서");

    @JsonValue
    private final String value;

    public static CommunicationStyle fromValue(String value) {
        if (value == null) return null;

        for (CommunicationStyle style : values()) {
            if (style.value.equals(value)) {
                return style;
            }
        }
        throw new IllegalArgumentException("Invalid communication style: " + value);
    }
}