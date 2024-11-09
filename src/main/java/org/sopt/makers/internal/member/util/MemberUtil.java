package org.sopt.makers.internal.member.util;

import java.util.List;

public class MemberUtil {

    public static List<Integer> extractGenerations(List<String> activitiesAndGeneration) {
        return activitiesAndGeneration.stream()
                .map(item -> Integer.parseInt(item.split(" ")[0].replaceAll("[^0-9]", "")))
                .toList();
    }

    public static List<String> extractActivities(List<String> activitiesAndGeneration) {
        return activitiesAndGeneration.stream()
                .map(item -> item.split(" ")[1])
                .toList();
    }
}
