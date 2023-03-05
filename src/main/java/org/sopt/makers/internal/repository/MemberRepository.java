package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByName(String name);
    List<Member> findAllByNameContaining(String name);
    Optional<Member> findByAuthUserId(String authId);

    Optional<Member> findByIdAndHasProfileTrue(Long id);

    List<Member> findAllByHasProfileTrue();

    List<Member> findAllByHasProfileTrueAndIdIn(List<Long> memberIds);
}
