package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.AnswerReaction;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnswerReactionRepository extends JpaRepository<AnswerReaction, Long> {

	Optional<AnswerReaction> findByAnswerAndMember(MemberAnswer answer, Member member);

	boolean existsByAnswerAndMember(MemberAnswer answer, Member member);

	@Query("SELECT COUNT(ar) FROM AnswerReaction ar WHERE ar.answer.id = :answerId")
	long countByAnswerId(@Param("answerId") Long answerId);

	void deleteByAnswerAndMember(MemberAnswer answer, Member member);
}
