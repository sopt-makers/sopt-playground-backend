package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.AnswerReaction;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnswerReactionRepository extends JpaRepository<AnswerReaction, Long> {

	Optional<AnswerReaction> findByAnswerAndMember(MemberAnswer answer, Member member);

	boolean existsByAnswerAndMember(MemberAnswer answer, Member member);

	@Query("""
		SELECT ar.answer.id AS answerId,
		       COUNT(ar) AS reactionCount
		FROM AnswerReaction ar
		WHERE ar.answer.id IN :answerIds
		GROUP BY ar.answer.id
	""")
	List<AnswerReactionCountProjection> countByAnswerIds(@Param("answerIds") List<Long> answerIds);

	@Query("""
		SELECT ar.answer.id
		FROM AnswerReaction ar
		WHERE ar.member.id = :memberId
		  AND ar.answer.id IN :answerIds
	""")
	List<Long> findReactedAnswerIds(@Param("memberId") Long memberId, @Param("answerIds") List<Long> answerIds);

	void deleteByAnswerAndMember(MemberAnswer answer, Member member);

	interface AnswerReactionCountProjection {
		Long getAnswerId();
		Long getReactionCount();
	}
}
