package org.sopt.makers.internal.sopmodam.repository;

import java.util.Optional;

import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestionVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SopmodamQuestionVoteRepository extends JpaRepository<SopmodamQuestionVote, Long> {

    boolean existsByRoundIdAndMemberId(Long roundId, Long memberId);

    @Query("""
        SELECT vote.question.id
        FROM SopmodamQuestionVote vote
        WHERE vote.round.id = :roundId
          AND vote.member.id = :memberId
    """)
    Optional<Long> findQuestionIdByRoundIdAndMemberId(
        @Param("roundId") Long roundId,
        @Param("memberId") Long memberId
    );
}
