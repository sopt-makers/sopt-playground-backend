package org.sopt.makers.internal.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionCleaner {
    private static final Pattern mentionPattern = Pattern.compile("@(.*?)\\[\\d+\\]");

    public static String removeMentionIds(String content) {
        Matcher matcher = mentionPattern.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String nickname = matcher.group(1);
            matcher.appendReplacement(sb, "@" + nickname);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

