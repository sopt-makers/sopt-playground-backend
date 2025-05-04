package org.sopt.makers.internal.member.repository.career;

import org.sopt.makers.internal.member.domain.MemberCareer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCareerRepository extends JpaRepository<MemberCareer, Long>, MemberCareerRepositoryCustom {
}
