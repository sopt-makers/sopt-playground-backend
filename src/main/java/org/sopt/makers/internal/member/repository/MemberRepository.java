package org.sopt.makers.internal.member.repository;

import java.util.List;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

//    List<Member> findAllByNameContaining(String name);

    List<Member> findAllByIdIn(List<Long> ids);

    List<Member> findAllByHasProfileTrueAndIdIn(List<Long> memberIds);

    @Query(value = "SELECT m.id FROM users m WHERE m.appjam_tl_generation = :generation ORDER BY RANDOM()", nativeQuery = true)
    List<Long> findAppjamTlMemberIdsByGenerationRandomly(@Param("generation") Integer generation);
}
