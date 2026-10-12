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
}
