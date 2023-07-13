package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.soulmate.SoulmateMissionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoulmateMissionHistoryRepository extends JpaRepository<SoulmateMissionHistory, Long> {
    List<SoulmateMissionHistory> findAllBySoulmateIdOrSoulmateIdOrderBySentAtAsc(Long mySoulmateId, Long opponentSoulmateId);
}
