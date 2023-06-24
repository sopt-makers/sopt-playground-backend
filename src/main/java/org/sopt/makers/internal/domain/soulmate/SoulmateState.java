package org.sopt.makers.internal.domain.soulmate;

public enum SoulmateState {
    EMPTY(null);

    final String state;
    SoulmateState(String state) {
        this.state = state;
    }
}
