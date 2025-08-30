package org.sopt.makers.internal.member.dto.response;

import org.sopt.makers.internal.external.platform.InternalUserDetails;

public record MemberBlockResponse(
        Boolean status,
        BlockMemberInfo blockingMember,
        BlockMemberInfo blockedMember
) {

  public record BlockMemberInfo(
          Long id,
          String name
  ) { }

  public static MemberBlockResponse of (Boolean status, InternalUserDetails blockingMember, InternalUserDetails blockedMember) {
    return new MemberBlockResponse(
            status,
            new BlockMemberInfo(blockingMember.userId(), blockingMember.name()),
            new BlockMemberInfo(blockedMember.userId(), blockedMember.name())
    );
  }
}
