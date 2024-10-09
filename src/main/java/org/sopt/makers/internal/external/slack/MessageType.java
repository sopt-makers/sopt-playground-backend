package org.sopt.makers.internal.external.slack;

import lombok.Getter;

@Getter
public enum MessageType {
    INFO("#36a64f", "info"),
    CLIENT("#ffff00", "Client error"),
    SERVER("#ff0000", "Internal server error"),
    ;

    final String color;
    final String title;

    MessageType(String color, String title) {
        this.color = color;
        this.title = title;
    }
}
