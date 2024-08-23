package org.sopt.makers.internal.external.slack;

import lombok.Getter;

@Getter
public enum MessageType {
    INFO("#36a64f"),
    CLIENT("#ffff00"),
    SERVER("#ff0000"),
    ;

    final String color;

    MessageType(String color) {
        this.color = color;
    }
}
