package org.sopt.makers.internal.member.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkTime {
    MORNING("아침"),
    NIGHT("밤");

    @JsonValue
    private final String value;

    public static WorkTime fromValue(String value) {
        if (value == null) return null;

        for (WorkTime time : values()) {
            if (time.value.equals(value)) {
                return time;
            }
        }
        throw new IllegalArgumentException("Invalid work time: " + value);
    }
}