package org.sopt.makers.internal.sopmodam.repository;

import java.util.Optional;

import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SopmodamAnswerRepository extends JpaRepository<SopmodamAnswer, Long> {

    @Query("SELECT COUNT(answer) FROM SopmodamAnswer answer WHERE answer.question.round.id = :roundId")
    long countByRoundId(@Param("roundId") Long roundId);

    @Query("""
        SELECT CASE WHEN COUNT(answer) > 0 THEN true ELSE false END
        FROM SopmodamAnswer answer
        WHERE answer.question.round.id = :roundId
          AND answer.member.id = :memberId
    """)
    boolean existsByRoundIdAndMemberId(
        @Param("roundId") Long roundId,
        @Param("memberId") Long memberId
    );

    @Query("""
        SELECT answer
        FROM SopmodamAnswer answer
        WHERE answer.question.round.id = :roundId
          AND answer.member.id = :memberId
    """)
    Optional<SopmodamAnswer> findByRoundIdAndMemberId(
        @Param("roundId") Long roundId,
        @Param("memberId") Long memberId
    );
}
