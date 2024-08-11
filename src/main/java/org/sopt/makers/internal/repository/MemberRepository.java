package org.sopt.makers.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sopt.makers.internal.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByName(String name);
    Optional<Member> findByPhone(String phone);

    Optional<Member> findByAuthUserId(String authId);

    Optional<Member> findByIdAndHasProfileTrue(Long id);

    List<Member> findAllByNameContaining(String name);

    List<Member> findAllByHasProfileTrue();

    List<Member> findAllByIsCoffeeChatActivateTrue();

    List<Member> findAllByHasProfileTrueAndIdIn(List<Long> memberIds);
    Long countByIdIn(Set<Long> memberIds);
}
