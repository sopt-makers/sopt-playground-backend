package org.sopt.makers.internal.resolution.repository;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserResolutionRepository extends JpaRepository<UserResolution, Long> {

    // READ
    Optional<UserResolution> findUserResolutionByMemberAndGeneration(Member member, int generation);

    boolean existsByMemberAndGeneration(Member member, int generation);
}
