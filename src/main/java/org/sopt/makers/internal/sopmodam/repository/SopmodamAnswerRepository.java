package org.sopt.makers.internal.sopmodam.repository;

import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SopmodamAnswerRepository extends JpaRepository<SopmodamAnswer, Long> {

    @Query("SELECT COUNT(answer) FROM SopmodamAnswer answer WHERE answer.question.round.id = :roundId")
    long countByRoundId(@Param("roundId") Long roundId);
}
