package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberAnswerRepository extends JpaRepository<MemberAnswer, Long> {

	Optional<MemberAnswer> findByQuestion(MemberQuestion question);

	boolean existsByQuestion(MemberQuestion question);
}
