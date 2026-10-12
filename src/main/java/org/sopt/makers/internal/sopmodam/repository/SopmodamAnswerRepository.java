package org.sopt.makers.internal.sopmodam.repository;

import java.util.Optional;

import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SopmodamAnswerRepository extends JpaRepository<SopmodamAnswer, Long> {

    boolean existsByQuestionIdAndMemberId(Long questionId, Long memberId);

    @Query("SELECT COUNT(answer) FROM SopmodamAnswer answer WHERE answer.question.round.id = :roundId")
    long countByRoundId(@Param("roundId") Long roundId);

    // member, question, round 를 한 번에 로드해 서비스에서 지연 로딩 없이 접근할 수 있도록 한다
    @Query("SELECT a FROM SopmodamAnswer a JOIN FETCH a.member JOIN FETCH a.question q JOIN FETCH q.round WHERE a.id = :answerId")
    Optional<SopmodamAnswer> findByIdWithDetails(@Param("answerId") Long answerId);
}
