package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.domain.QuestionReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionReactionRepository extends JpaRepository<QuestionReaction, Long> {

	Optional<QuestionReaction> findByQuestionAndMember(MemberQuestion question, Member member);

	boolean existsByQuestionAndMember(MemberQuestion question, Member member);

	@Query("""
		SELECT qr.question.id AS questionId,
		       COUNT(qr) AS reactionCount
		FROM QuestionReaction qr
		WHERE qr.question.id IN :questionIds
		GROUP BY qr.question.id
	""")
	List<QuestionReactionCountProjection> countByQuestionIds(@Param("questionIds") List<Long> questionIds);

	@Query("""
		SELECT qr.question.id
		FROM QuestionReaction qr
		WHERE qr.member.id = :memberId
		  AND qr.question.id IN :questionIds
	""")
	List<Long> findReactedQuestionIds(@Param("memberId") Long memberId, @Param("questionIds") List<Long> questionIds);

	void deleteByQuestionAndMember(MemberQuestion question, Member member);

	interface QuestionReactionCountProjection {
		Long getQuestionId();
		Long getReactionCount();
	}
}
