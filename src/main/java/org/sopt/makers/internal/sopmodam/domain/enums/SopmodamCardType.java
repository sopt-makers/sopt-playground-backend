package org.sopt.makers.internal.sopmodam.domain.enums;

import java.util.Optional;

public enum SopmodamCardType {

    VOTE,
    ANSWER_PREPARING,
    ANSWER_COLLECTING,
    REVEALED;

    // 홈 카드 노출 매트릭스. 카드를 보여주지 않는 조합이면 비어 있다
    public static Optional<SopmodamCardType> of(SopmodamPhase phase, SopmodamMemberType memberType) {
        if (memberType == SopmodamMemberType.NOT_TARGET) {
            return Optional.empty();
        }

        return switch (phase) {
            case SCHEDULED -> Optional.empty();
            case VOTING -> memberType == SopmodamMemberType.HONORARY
                ? Optional.empty()
                : Optional.of(VOTE);
            case ANSWERING -> memberType == SopmodamMemberType.ACTIVE_YB
                ? Optional.of(ANSWER_PREPARING)
                : Optional.of(ANSWER_COLLECTING);
            case REVEALED -> Optional.of(REVEALED);
        };
    }
}
