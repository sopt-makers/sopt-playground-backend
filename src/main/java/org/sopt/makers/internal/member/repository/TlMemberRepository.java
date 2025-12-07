package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.TlMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TlMemberRepository extends JpaRepository<TlMember, Long> {
    List<TlMember> findByTlGeneration(Integer tlGeneration);
}
