package org.sopt.makers.internal.deprecated.soulmate.repository;

import org.sopt.makers.internal.deprecated.soulmate.domain.SoulmateMissionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoulmateMissionHistoryRepository extends JpaRepository<SoulmateMissionHistory, Long> {
    List<SoulmateMissionHistory> findAllBySoulmateIdOrSoulmateIdOrderBySentAtAsc(Long mySoulmateId, Long opponentSoulmateId);
}
