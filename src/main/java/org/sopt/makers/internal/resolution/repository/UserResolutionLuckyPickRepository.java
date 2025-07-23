package org.sopt.makers.internal.resolution.repository;

import java.util.Optional;
import org.sopt.makers.internal.resolution.domain.UserResolutionLuckyPick;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserResolutionLuckyPickRepository extends JpaRepository<UserResolutionLuckyPick, Long> {
    Optional<UserResolutionLuckyPick> findByMemberId(Long memberId);
    boolean existsByMemberIdAndHasDrawnTrue(Long memberId);
    long count();
}
