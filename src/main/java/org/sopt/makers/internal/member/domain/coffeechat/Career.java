package org.sopt.makers.internal.member.domain.coffeechat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Career {

    NONE("아직 없어요"),
    INTERN("인턴 경험만 있어요"),
    JUNIOR("주니어 (0-3년)"),
    MIDDLE("미들 (4-8년)"),
    SENIOR("시니어 (9년 이상)")
    ;

    private final String title;
}
