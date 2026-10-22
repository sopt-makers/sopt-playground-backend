package org.sopt.makers.internal.sopmodam.service;

import org.sopt.makers.internal.exception.ForbiddenException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamMemberType;
import org.springframework.stereotype.Component;

@Component
public class SopmodamMemberPolicy {

    // 경험담 알림은 이 기수 이후 명예기수까지만 보낸다. 답변 권한(canAnswer)과는 별개다
    public static final int ANSWER_NOTIFICATION_MIN_GENERATION = 34;

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

    public boolean isVoteNotificationTarget(InternalUserDetails user, int roundGeneration) {
        return resolveType(user, roundGeneration).isCanVote();
    }

    public boolean isAnswerNotificationTarget(InternalUserDetails user, int roundGeneration) {
        SopmodamMemberType memberType = resolveType(user, roundGeneration);
        if (memberType == SopmodamMemberType.HONORARY) {
            return user.lastGeneration() >= ANSWER_NOTIFICATION_MIN_GENERATION;
        }
        return memberType.isCanAnswer();
    }

    public void validateCanVote(SopmodamMemberType memberType) {
        if (!memberType.isCanVote()) {
            throw new ForbiddenException("질문 투표는 활동 기수만 참여할 수 있습니다.");
        }
    }

    public void validateCanAnswer(SopmodamMemberType memberType) {
        if (!memberType.isCanAnswer()) {
            throw new ForbiddenException("답변 권한이 없습니다.");
        }
    }
}
