package org.sopt.makers.internal.sopmodam.domain.enums;

// 솝모담 장려 알림 대상 유형
public enum SopmodamNotificationTargetType {

    // 투표 대상 활동 YB·OB 중 이 회차에 투표하지 않은 사용자
    NOT_VOTED,
    // 경험담 작성 대상인 활동 OB·명예기수
    ANSWER_TARGET,
    // 경험담 작성 대상 중 이 회차에 답변하지 않은 사용자
    NOT_ANSWERED
}
