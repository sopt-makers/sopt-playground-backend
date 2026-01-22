package org.sopt.makers.internal.member.constants;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.sopt.makers.internal.member.domain.enums.Part;
import org.springframework.stereotype.Component;

@Component
public class AskMemberId {
    // Dev 환경용 멤버 ID
    private static final Map<Part, List<Long>> DEV_ASK_MEMBERS = Map.of(
            Part.SERVER, List.of(929L, 209L),
            Part.IOS, List.of(192L),
            Part.ANDROID, List.of(223L),
            Part.WEB, List.of(945L),
            Part.DESIGN, List.of(930L),
            Part.PLAN, List.of(229L)
    );

    // Production 환경용 멤버 ID
    private static final Map<Part, List<Long>> PROD_ASK_MEMBERS = Map.of(
            Part.PLAN, List.of(319L, 11L, 661L, 769L),
            Part.DESIGN, List.of(144L, 358L, 64L, 210L),
            Part.WEB, List.of(84L, 85L, 860L, 784L, 673L, 574L, 115L, 635L),
            Part.IOS, List.of(35L, 45L, 22L),
            Part.ANDROID, List.of(585L, 21L, 407L),
            Part.SERVER, List.of(647L, 989L, 306L, 60L, 221L, 293L)
    );

    public List<Long> getAskMembersByPart(Part part, boolean isProd) {
        Map<Part, List<Long>> askMembersByPart = isProd ? PROD_ASK_MEMBERS : DEV_ASK_MEMBERS;
        return askMembersByPart.getOrDefault(part, List.of());
    }

    public List<Long> getAllAskMembers(boolean isProd) {
        Map<Part, List<Long>> askMembersByPart = isProd ? PROD_ASK_MEMBERS : DEV_ASK_MEMBERS;
        return askMembersByPart.values().stream()
                .flatMap(List::stream)
                .toList();
    }
}
