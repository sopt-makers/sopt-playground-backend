package org.sopt.makers.internal.resolution.repository;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserResolutionRepository extends JpaRepository<UserResolution, Long> {

    // READ
    Optional<UserResolution> findUserResolutionByMember(Member member);

    @Query("select count(ur.id) from UserResolution ur where ur.member=:member and ur.member.generation=:generation")
    int countByMember(Member member, int generation);
}
