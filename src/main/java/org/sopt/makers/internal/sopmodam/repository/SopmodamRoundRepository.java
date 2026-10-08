package org.sopt.makers.internal.sopmodam.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SopmodamRoundRepository extends JpaRepository<SopmodamRound, Long> {

    List<SopmodamRound> findAllByOrderByVoteStartAtDesc();

    // [voteStartAt, answerEndAt) 반열린 구간끼리 겹치는지 확인한다. 한쪽 끝과 다른 쪽 시작이 맞닿는 것은 겹침이 아니다
    @Query("""
        SELECT CASE WHEN COUNT(round) > 0 THEN true ELSE false END
        FROM SopmodamRound round
        WHERE round.voteStartAt < :answerEndAt
          AND round.answerEndAt > :voteStartAt
    """)
    boolean existsOverlapping(
        @Param("voteStartAt") LocalDateTime voteStartAt,
        @Param("answerEndAt") LocalDateTime answerEndAt
    );

    // 회차 수정 시 수정 대상 회차 자신은 겹침 검사에서 제외한다
    @Query("""
        SELECT CASE WHEN COUNT(round) > 0 THEN true ELSE false END
        FROM SopmodamRound round
        WHERE round.id <> :roundId
          AND round.voteStartAt < :answerEndAt
          AND round.answerEndAt > :voteStartAt
    """)
    boolean existsOverlappingExcept(
        @Param("roundId") Long roundId,
        @Param("voteStartAt") LocalDateTime voteStartAt,
        @Param("answerEndAt") LocalDateTime answerEndAt
    );
}
