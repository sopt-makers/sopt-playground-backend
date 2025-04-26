package org.sopt.makers.internal.member.repository.soptactivity;

import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberSoptActivityRepository extends JpaRepository<MemberSoptActivity, Long> {

    Optional<MemberSoptActivity> findByIdAndMemberId(Long id, Long memberId);
    List<MemberSoptActivity> findAllByMemberId(Long memberId);
    List<MemberSoptActivity> findAllByMemberIdIn(List<Long> memberIds);
    MemberSoptActivity findTop1ByMemberIdOrderByGenerationDesc(Long memberId);
}