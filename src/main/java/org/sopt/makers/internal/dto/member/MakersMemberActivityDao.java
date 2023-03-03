package org.sopt.makers.internal.dto.member;

import com.querydsl.core.annotations.QueryProjection;

public record MakersMemberActivityDao(

    Long id,
    Long memberId,
    Long teamId,
    String part,
    Integer generation,
    String makersTeamName,
    String makersTeamDesc
) {
  @QueryProjection
  public MakersMemberActivityDao {}
}
