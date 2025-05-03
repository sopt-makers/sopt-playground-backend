package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberBlockRepository extends JpaRepository<MemberBlock, Long> {

    // READ
    Optional<MemberBlock> findByBlockerAndBlockedMember(Member blocker, Member blocked);

    Boolean existsByBlockerAndBlockedMember(Member blocker, Member blocked);
}
