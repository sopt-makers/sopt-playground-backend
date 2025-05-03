package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.SoptMemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoptMemberHistoryRepository extends JpaRepository<SoptMemberHistory, Long> {
    List<SoptMemberHistory> findAllByEmailOrderByIdDesc(String email);
    Optional<SoptMemberHistory> findTopByEmailOrderByIdDesc(String email);
    Optional<SoptMemberHistory> findTopByPhoneOrderByIdDesc(String phone);
    List<SoptMemberHistory> findAllByPhoneOrderByIdDesc(String phone);
}
