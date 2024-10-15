package org.sopt.makers.internal.member.domain.coffeechat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.internal.exception.ClientBadRequestException;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MeetingType {

    ONLINE("온라인"),
    OFFLINE("오프라인"),
    ANYTHING("무관")
    ;

    final String value;

    @JsonCreator
    public static MeetingType fromValue(String value) {
        return Arrays.stream(MeetingType.values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new ClientBadRequestException("Unknown Meeting Type Value: " + value));
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
