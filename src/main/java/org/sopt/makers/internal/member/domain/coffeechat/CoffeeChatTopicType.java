package org.sopt.makers.internal.member.domain.coffeechat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CoffeeChatTopicType {

    STARTUP("창업"),
    CAREER("커리어"),
    PORTFOLIO("포트폴리오"),
    RESUME("이력서/자소서"),
    MEETING("면접"),
    PROFESSIONALISM("직무 전문성"),
    IMPROVEMENT("자기계발"),
    NETWORKING("네트워킹"),
    PROJECT("프로젝트"),
    ETC("기타")
    ;

    private final String title;
}
