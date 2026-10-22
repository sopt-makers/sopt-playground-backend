package org.sopt.makers.internal.sopmodam.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;

import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamNotificationTargetType;

public final class NotificationTargetCsvUtil {

    private static final String HEADER = "user_id";
    private static final DateTimeFormatter FILE_NAME_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private NotificationTargetCsvUtil() {
    }

    // 알림 발송 템플릿 형식을 그대로 따른다: 헤더 user_id, 행마다 끝에 쉼표, LF 줄바꿈, 마지막 줄 개행 없음
    public static String toCsv(List<Long> userIds) {
        StringJoiner csv = new StringJoiner("\n");
        csv.add(HEADER);
        userIds.forEach(userId -> csv.add(userId + ","));
        return csv.toString();
    }

    // 예: sopmodam-round3-not-voted-20261022-1530.csv
    public static String toFileName(Long roundId, SopmodamNotificationTargetType type, LocalDateTime now) {
        String typeName = type.name().toLowerCase().replace('_', '-');
        return String.format("sopmodam-round%d-%s-%s.csv", roundId, typeName, now.format(FILE_NAME_TIME_FORMAT));
    }
}
