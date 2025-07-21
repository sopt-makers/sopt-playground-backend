package org.sopt.makers.internal.resolution.repository;

import org.sopt.makers.internal.resolution.domain.UserResolutionLuckyPick;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserResolutionLuckyPickRepository extends JpaRepository<UserResolutionLuckyPick, Long> {
    boolean existsByMemberIdAndHasDrawnTrue(Long memberId);
}
