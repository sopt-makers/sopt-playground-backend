package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.MemberCareer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberCareerRepository extends JpaRepository<MemberCareer, Long> {
    Optional<MemberCareer> findByIdAndMemberId(Long id, Long memberId);
}
