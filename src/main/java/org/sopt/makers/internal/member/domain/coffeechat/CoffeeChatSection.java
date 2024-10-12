package org.sopt.makers.internal.member.domain.coffeechat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CoffeeChatSection {

    SOPT_ACTIVITY("SOPT 활동"),
    PLAN("기획"),
    DESIGN("디자인"),
    FRONTEND("프론트엔드"),
    BACKEND("백엔드"),
    APP("앱 개발"),
    ETC("기타")
    ;

    private final String title;
}
