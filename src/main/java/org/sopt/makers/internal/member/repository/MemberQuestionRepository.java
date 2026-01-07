package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberQuestionRepository extends JpaRepository<MemberQuestion, Long> {

	@Query("SELECT q FROM MemberQuestion q " +
		"WHERE q.receiver.id = :receiverId " +
		"AND EXISTS (SELECT 1 FROM MemberAnswer a WHERE a.question.id = q.id) " +
		"ORDER BY q.createdAt DESC")
	List<MemberQuestion> findAnsweredQuestions(
		@Param("receiverId") Long receiverId,
		Pageable pageable
	);

	@Query("SELECT q FROM MemberQuestion q " +
		"WHERE q.receiver.id = :receiverId " +
		"AND NOT EXISTS (SELECT 1 FROM MemberAnswer a WHERE a.question.id = q.id) " +
		"ORDER BY q.createdAt DESC")
	List<MemberQuestion> findUnansweredQuestions(
		@Param("receiverId") Long receiverId,
		Pageable pageable
	);

	@Query("SELECT COUNT(q) FROM MemberQuestion q " +
		"WHERE q.receiver.id = :receiverId " +
		"AND EXISTS (SELECT 1 FROM MemberAnswer a WHERE a.question.id = q.id)")
	long countAnsweredQuestions(@Param("receiverId") Long receiverId);

	@Query("SELECT COUNT(q) FROM MemberQuestion q " +
		"WHERE q.receiver.id = :receiverId " +
		"AND NOT EXISTS (SELECT 1 FROM MemberAnswer a WHERE a.question.id = q.id)")
	long countUnansweredQuestions(@Param("receiverId") Long receiverId);

	boolean existsByIdAndAsker(Long questionId, Member asker);

	boolean existsByIdAndReceiver(Long questionId, Member receiver);

	@Query("SELECT q FROM MemberQuestion q ORDER BY q.id DESC")
	List<MemberQuestion> findTopByOrderByIdDesc(Pageable pageable);

	@Query("SELECT q FROM MemberQuestion q WHERE q.receiver.id = :receiverId")
	List<MemberQuestion> findByReceiverId(@Param("receiverId") Long receiverId);
}
