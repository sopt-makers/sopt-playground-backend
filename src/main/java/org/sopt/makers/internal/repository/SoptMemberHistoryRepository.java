package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.SoptMemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SoptMemberHistoryRepository extends JpaRepository<SoptMemberHistory, Long> {
    Optional<SoptMemberHistory> findByEmail(String email);
}
