package org.sopt.makers.internal.sopmodam.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SopmodamMemberType {

    ACTIVE_YB(true, false),
    ACTIVE_OB(true, true),
    HONORARY(false, true),
    // 회차 기수보다 뒤 기수인 회원
    NOT_TARGET(false, false);

    private final boolean canVote;
    private final boolean canAnswer;
}
