package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberQuestionRepository extends JpaRepository<MemberQuestion, Long> {

	@Query("SELECT q FROM MemberQuestion q " +
		"WHERE q.receiver.id = :receiverId " +
		"AND q.answer IS NOT NULL " +
		"AND (:cursor IS NULL OR q.id < :cursor) " +
		"ORDER BY q.createdAt DESC")
	List<MemberQuestion> findAnsweredQuestions(
		@Param("receiverId") Long receiverId,
		@Param("cursor") Long cursor,
		@Param("limit") int limit
	);

	@Query("SELECT q FROM MemberQuestion q " +
		"WHERE q.receiver.id = :receiverId " +
		"AND q.answer IS NULL " +
		"AND (:cursor IS NULL OR q.id < :cursor) " +
		"ORDER BY q.createdAt DESC")
	List<MemberQuestion> findUnansweredQuestions(
		@Param("receiverId") Long receiverId,
		@Param("cursor") Long cursor,
		@Param("limit") int limit
	);

	@Query("SELECT COUNT(q) FROM MemberQuestion q " +
		"WHERE q.receiver.id = :receiverId " +
		"AND q.answer IS NULL")
	long countUnansweredQuestions(@Param("receiverId") Long receiverId);

	boolean existsByIdAndAsker(Long questionId, Member asker);

	boolean existsByIdAndReceiver(Long questionId, Member receiver);
}
