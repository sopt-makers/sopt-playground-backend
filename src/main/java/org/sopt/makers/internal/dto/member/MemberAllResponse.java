package org.sopt.makers.internal.dto.member;

import java.util.List;

public record MemberAllResponse(
        List<MemberResponse> mebers,
        Boolean hasNext
) { }
