package org.sopt.makers.internal.coffeechat.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.internal.exception.BadRequestException;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Career {

    NONE("아직 없어요"),
    INTERN("인턴 경험만 있어요"),
    JUNIOR("주니어 (0-3년)"),
    MIDDLE("미들 (4-8년)"),
    SENIOR("시니어 (9년 이상)"),
    STARTUP("창업 중")
    ;

    private final String title;

    @JsonCreator
    public static Career fromTitle(String title) {
        return Arrays.stream(Career.values())
                .filter(career -> career.title.equals(title))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unknown Career Title: " + title));
    }

    @JsonValue
    public String getTitle() {
        return title;
    }
}
