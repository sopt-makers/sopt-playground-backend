package org.sopt.makers.internal.member.domain.coffeechat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.internal.exception.ClientBadRequestException;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Career {

    NONE("없음"),
    INTERN("인턴"),
    JUNIOR("주니어"),
    MIDDLE("미들"),
    SENIOR("시니어")
    ;

    private final String title;

    @JsonCreator
    public static Career fromTitle(String title) {
        return Arrays.stream(Career.values())
                .filter(career -> career.title.equals(title))
                .findFirst()
                .orElseThrow(() -> new ClientBadRequestException("Unknown Career Title: " + title));
    }

    @JsonValue
    public String getTitle() {
        return title;
    }
}
