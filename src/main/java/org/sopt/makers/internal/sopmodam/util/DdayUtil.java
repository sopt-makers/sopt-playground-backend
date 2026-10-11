package org.sopt.makers.internal.sopmodam.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class DdayUtil {

    private DdayUtil() {
    }

    // (마감 시각 - 1초)의 날짜 - 오늘 날짜. 마감이 지났으면 null
    public static Integer calculateDDay(LocalDateTime deadline, LocalDateTime now) {
        if (!now.isBefore(deadline)) {
            return null;
        }
        return (int) ChronoUnit.DAYS.between(now.toLocalDate(), deadline.minusSeconds(1).toLocalDate());
    }
}
