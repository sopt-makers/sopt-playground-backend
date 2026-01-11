package org.sopt.makers.internal.member.constants;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.sopt.makers.internal.member.domain.enums.Part;

public class AskMemberId {
    @Getter
    private static final Map<Part, List<Long>> askMembersByPart = Map.of(
            Part.SERVER, List.of(929L,209L),
            Part.IOS, List.of(192L),
            Part.ANDROID, List.of(223L),
            Part.WEB, List.of(945L),
            Part.DESIGN, List.of(930L),
            Part.PLAN, List.of(229L)
    );

    public static List<Long> getAskMembersByPart(Part part) {
        return askMembersByPart.getOrDefault(part, List.of());
    }

    public static List<Long> getAllAskMembers() {
        return askMembersByPart.values().stream()
                .flatMap(List::stream)
                .toList();
    }
}
