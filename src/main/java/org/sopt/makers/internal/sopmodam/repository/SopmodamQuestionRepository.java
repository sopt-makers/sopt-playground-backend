package org.sopt.makers.internal.sopmodam.repository;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SopmodamQuestionRepository extends JpaRepository<SopmodamQuestion, Long> {

    List<SopmodamQuestion> findAllByRoundIdOrderByIdAsc(Long roundId);

    Optional<SopmodamQuestion> findByIdAndRoundId(Long id, Long roundId);

    @Modifying
    @Query("UPDATE SopmodamQuestion question SET question.voteCount = question.voteCount + 1 WHERE question.id = :questionId")
    int increaseVoteCount(@Param("questionId") Long questionId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE SopmodamQuestion q
        SET q.isSelected = CASE WHEN q.id = :questionId THEN true ELSE false END
        WHERE q.round.id = :roundId
    """)
    void updateSelectedQuestionInRound(@Param("roundId") Long roundId, @Param("questionId") Long questionId);

    // 회차 내 모든 질문을 미선정 상태로 초기화한다. clearAutomatically = true 로 영속성 컨텍스트를 비워 후속 merge 가 정확히 반영되도록 한다
    @Modifying(clearAutomatically = true)
    @Query("UPDATE SopmodamQuestion q SET q.isSelected = false WHERE q.round.id = :roundId")
    void deselectAllByRoundId(@Param("roundId") Long roundId);
}
