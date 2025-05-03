package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.MemberLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberLinkRepository extends JpaRepository<MemberLink, Long> {
    Optional<MemberLink> findByIdAndMemberId(Long id, Long memberId);
}
