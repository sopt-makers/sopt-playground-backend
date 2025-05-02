package org.sopt.makers.internal.coffeechat.domain.enums;

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
    ANYTHING("온/오프라인")
    ;

    final String title;

    @JsonCreator
    public static MeetingType fromTitle(String title) {
        return Arrays.stream(MeetingType.values())
                .filter(type -> type.title.equals(title))
                .findFirst()
                .orElseThrow(() -> new ClientBadRequestException("Unknown Meeting Type Title: " + title));
    }

    @JsonValue
    public String getTitle() {
        return title;
    }
}
