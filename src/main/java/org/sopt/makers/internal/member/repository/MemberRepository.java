package org.sopt.makers.internal.member.repository;

import java.util.List;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findAllByNameContaining(String name);

    List<Member> findAllByIdIn(List<Long> ids);

    List<Member> findAllByHasProfileTrueAndIdIn(List<Long> memberIds);
}
