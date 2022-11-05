package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.MemberLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberLinkRepository extends JpaRepository<MemberLink, Long> {
    Optional<MemberLink> findByIdAndMemberId(Long id, Long memberId);
}
