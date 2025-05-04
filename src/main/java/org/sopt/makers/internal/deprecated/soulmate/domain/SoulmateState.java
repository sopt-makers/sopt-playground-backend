package org.sopt.makers.internal.deprecated.soulmate.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SoulmateState {
    MatchingReady("MatchingReady"),
    MissionOpen("MissionOpen"),
    OpponentCompleted("OpponentCompleted"),
    SelfCompleted("SelfCompleted"),
    BothCompleted("BothCompleted"),
    OpenProfile("OpenProfile"),
    Disconnected("Disconnected");

    final String state;
    SoulmateState(String state) {
        this.state = state;
    }
    public static SoulmateState of(String state) {
        return Arrays.stream(SoulmateState.values())
                .filter(v -> v.getState().equals(state))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("울랄라~"));
    }
}
