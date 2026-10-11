package org.sopt.makers.internal.sopmodam.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SopmodamRoundRepository extends JpaRepository<SopmodamRound, Long> {

    Optional<SopmodamRound> findFirstByVoteStartAtLessThanEqualOrderByVoteStartAtDesc(LocalDateTime now);
}
