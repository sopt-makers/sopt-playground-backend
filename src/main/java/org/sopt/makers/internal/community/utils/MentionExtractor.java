package org.sopt.makers.internal.community.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionExtractor {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@.*?\\[(\\d+)]");
    private static final Pattern ANONYMOUS_PATTERN = Pattern.compile("@([가-힣]+\\s+[가-힣]+)(?!\\[)");

    private MentionExtractor() {
        throw new AssertionError("유틸 클래스는 인스턴스화 될 수 없습니다.");
    }

    public static Set<Long> extractMentionedUserIds(String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptySet();
        }

        Set<Long> userIds = new HashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);

        while (matcher.find()) {
            try {
                Long userId = Long.parseLong(matcher.group(1));
                userIds.add(userId);
            } catch (NumberFormatException e) {
                // 파싱 실패 시 무시 (잘못된 형식)
            }
        }

        return userIds;
    }

    public static Long[] getNewlyAddedMentions(String oldContent, Long[] newMentionIds) {
        if (newMentionIds == null || newMentionIds.length == 0) {
            return new Long[0];
        }

        Set<Long> oldMentions = extractMentionedUserIds(oldContent);

        return Arrays.stream(newMentionIds)
                .distinct()
                .filter(id -> !oldMentions.contains(id))
                .toArray(Long[]::new);
    }

    public static Set<String> extractAnonymousNicknames(String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptySet();
        }

        Set<String> nicknames = new HashSet<>();
        Matcher matcher = ANONYMOUS_PATTERN.matcher(content);

        while (matcher.find()) {
            String nickname = matcher.group(1).trim();
            if (!nickname.isEmpty()) {
                nicknames.add(nickname);
            }
        }

        return nicknames;
    }

    public static String[] getNewlyAddedAnonymousMentions(String oldContent, String[] newAnonymousNicknames) {

        if (newAnonymousNicknames == null || newAnonymousNicknames.length == 0) {
            return new String[0];
        }

        Set<String> oldNicknames = extractAnonymousNicknames(oldContent);

        return Arrays.stream(newAnonymousNicknames)
                .distinct()
                .filter(nickname -> !oldNicknames.contains(nickname))
                .toArray(String[]::new);
    }
}
