package org.sopt.makers.internal.repository.member;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.member.MemberBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberBlockRepository extends JpaRepository<MemberBlock, Long> {

    // READ
    Optional<MemberBlock> findByBlockerAndBlockedMember(Member blocker, Member blocked);

    Boolean existsByBlockerAndBlockedMember(Member blocker, Member blocked);
}
