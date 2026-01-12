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
			"AND q.answer IS NOT NULL " + // 기존 EXISTS를 필드 직접 참조로 간소화
			"ORDER BY q.createdAt DESC")
	List<MemberQuestion> findAnsweredQuestions(
			@Param("receiverId") Long receiverId,
			Pageable pageable
	);

	@Query("SELECT q FROM MemberQuestion q " +
			"WHERE q.receiver.id = :receiverId " +
			"AND q.answer IS NULL " +
			"ORDER BY q.createdAt DESC")
	List<MemberQuestion> findUnansweredQuestions(
			@Param("receiverId") Long receiverId,
			Pageable pageable
	);

	@Query("SELECT COUNT(q) FROM MemberQuestion q " +
			"WHERE q.receiver.id = :receiverId " +
			"AND q.answer IS NOT NULL")
	long countAnsweredQuestions(@Param("receiverId") Long receiverId);

	@Query("SELECT COUNT(q) FROM MemberQuestion q " +
			"WHERE q.receiver.id = :receiverId " +
			"AND q.answer IS NULL")
	long countUnansweredQuestions(@Param("receiverId") Long receiverId);

	@Query("SELECT q FROM MemberQuestion q " +
			"WHERE q.asker.id = :askerId AND q.receiver.id = :receiverId " +
			"AND q.answer IS NOT NULL " +
			"ORDER BY q.createdAt DESC")
	List<MemberQuestion> findAllAnsweredByAskerAndReceiverOrderByLatest(
			@Param("askerId") Long askerId,
			@Param("receiverId") Long receiverId
	);

	@Query("SELECT q.id FROM MemberQuestion q " +
			"WHERE q.receiver.id = :receiverId " +
			"AND q.answer IS NOT NULL " +
			"ORDER BY q.createdAt DESC")
	List<Long> findAllAnsweredQuestionIdsByReceiver(@Param("receiverId") Long receiverId);

	// ------------------

	boolean existsByIdAndAsker(Long questionId, Member asker);

	boolean existsByIdAndReceiver(Long questionId, Member receiver);

	@Query("SELECT q FROM MemberQuestion q ORDER BY q.id DESC")
	List<MemberQuestion> findTopByOrderByIdDesc(Pageable pageable);

	@Query("SELECT q FROM MemberQuestion q WHERE q.receiver.id = :receiverId")
	List<MemberQuestion> findByReceiverId(@Param("receiverId") Long receiverId);
}