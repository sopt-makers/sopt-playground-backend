package org.sopt.makers.internal.sopmodam.service;

import org.sopt.makers.internal.exception.ForbiddenException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamMemberType;
import org.springframework.stereotype.Component;

@Component
public class SopmodamMemberPolicy {

    // 기준 기수는 Constant.CURRENT_GENERATION 이 아니라 회차의 기수다
    public SopmodamMemberType resolveType(InternalUserDetails user, int roundGeneration) {
        if (user.lastGeneration() < roundGeneration) {
            return SopmodamMemberType.HONORARY;
        }
        if (user.lastGeneration() > roundGeneration) {
            return SopmodamMemberType.NOT_TARGET;
        }

        boolean hasPreviousSoptActivity = user.soptActivities().stream()
            .anyMatch(activity -> activity.isSopt() && activity.generation() < roundGeneration);
        return hasPreviousSoptActivity ? SopmodamMemberType.ACTIVE_OB : SopmodamMemberType.ACTIVE_YB;
    }

    public void validateCanVote(SopmodamMemberType memberType) {
        if (!memberType.isCanVote()) {
            throw new ForbiddenException("질문 투표는 활동 기수만 참여할 수 있습니다.");
        }
    }
}
