package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.domain.QuestionReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionReactionRepository extends JpaRepository<QuestionReaction, Long> {

	Optional<QuestionReaction> findByQuestionAndMember(MemberQuestion question, Member member);

	boolean existsByQuestionAndMember(MemberQuestion question, Member member);

	@Query("SELECT COUNT(qr) FROM QuestionReaction qr WHERE qr.question.id = :questionId")
	long countByQuestionId(@Param("questionId") Long questionId);

	void deleteByQuestionAndMember(MemberQuestion question, Member member);
}
