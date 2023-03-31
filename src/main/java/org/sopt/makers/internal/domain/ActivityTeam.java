package org.sopt.makers.internal.domain;

import java.util.Arrays;
import lombok.val;

public enum ActivityTeam {
    EMPTY(null),
    NO_TEAM("해당 없음"),
    MEDIA_TEAM("미디어팀"),
    OPERATION_TEAM("운영팀");

    final String teamName;
    ActivityTeam(String teamName) {
        this.teamName = teamName;
    }

    public static boolean hasActivityTeam(String team) {
        val rightTeam = Arrays.stream(ActivityTeam.values())
                .filter(v -> v.teamName.equals(team)).findAny();
        return rightTeam.isPresent();
    }
}
