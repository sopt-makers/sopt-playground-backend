package org.sopt.makers.internal.member.repository;

import java.util.List;

public interface MemberRepositoryCustom {

    List<Long> findTlMemberIdsByGenerationRandomly(Integer generation);
}
