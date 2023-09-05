package org.sopt.makers.internal.common;

import lombok.Getter;
import lombok.val;

import java.util.List;


public class MakersMemberId {
    private static final List<Long> makersMember = List.of(
            1L, 2L, 258L, 3L, 259L, 4L, 260L, 5L, 6L, 7L, 8L, 264L, 9L, 265L, 10L, 13L, 21L, 22L, 23L, 281L,
            26L, 282L, 283L, 28L, 29L, 285L, 30L, 286L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 294L, 39L, 40L, 554L,
            43L, 44L, 45L, 46L, 303L, 559L, 51L, 563L, 569L, 58L, 570L, 59L, 60L, 61L, 64L, 66L, 72L, 78L, 85L,
            99L, 357L, 358L, 107L, 112L, 370L, 115L, 128L, 390L, 396L, 143L, 144L, 401L, 410L, 171L, 173L, 180L,
            186L, 187L, 188L, 192L, 203L, 204L, 205L, 227L, 238L, 251L
    );

    public static List<Long> getMakersMember() {
        return makersMember;
    }
}
