package org.sopt.makers.internal.member.repository.career;

import org.sopt.makers.internal.member.domain.MemberCareer;

import java.util.Optional;

public interface MemberCareerRepositoryCustom {

    Optional<MemberCareer> findMemberLastCareerByMemberId(Long memberId);
}
