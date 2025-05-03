package org.sopt.makers.internal.member.dto.response;

import org.sopt.makers.internal.member.domain.Member;

public record MemberBlockResponse(
        Boolean status,
        BlockMemberInfo blockingMember,
        BlockMemberInfo blockedMember
) {

  public record BlockMemberInfo(
          Long id,
          String name
  ) { }

  public static MemberBlockResponse of (Boolean status, Member blockingMember, Member blockedMember) {
    return new MemberBlockResponse(
            status,
            new BlockMemberInfo(blockingMember.getId(), blockingMember.getName()),
            new BlockMemberInfo(blockedMember.getId(), blockedMember.getName())
    );
  }
}
