package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberQuestionRepository extends JpaRepository<MemberQuestion, Long> {

	@Query("SELECT q FROM MemberQuestion q " +
			"WHERE q.receiver.id = :receiverId " +
			"AND q.answer IS NOT NULL " +
			"ORDER BY q.createdAt DESC, q.id DESC")
	List<MemberQuestion> findAnsweredQuestions(
			@Param("receiverId") Long receiverId,
			Pageable pageable
	);

	@Query("SELECT q FROM MemberQuestion q " +
			"WHERE q.receiver.id = :receiverId " +
			"AND NOT EXISTS (SELECT a FROM MemberAnswer a WHERE a.question = q) " +
			"ORDER BY q.createdAt DESC, q.id DESC")
	List<MemberQuestion> findUnansweredQuestions(
			@Param("receiverId") Long receiverId,
			Pageable pageable
	);

	@Query("SELECT q FROM MemberQuestion q " +
			"WHERE q.receiver.id = :receiverId " +
			"ORDER BY q.createdAt DESC, q.id DESC")
	List<MemberQuestion> findAllQuestions(
			@Param("receiverId") Long receiverId,
			Pageable pageable
	);

	@Query("SELECT COUNT(q) FROM MemberQuestion q " +
			"WHERE q.receiver.id = :receiverId " +
			"AND q.answer IS NOT NULL")
	long countAnsweredQuestions(@Param("receiverId") Long receiverId);

	@Query("SELECT COUNT(q) FROM MemberQuestion q " +
			"WHERE q.receiver.id = :receiverId " +
			"AND NOT EXISTS (SELECT a FROM MemberAnswer a WHERE a.question = q)")
	long countUnansweredQuestions(@Param("receiverId") Long receiverId);

	@Query("SELECT COUNT(q) FROM MemberQuestion q " +
			"WHERE q.receiver.id = :receiverId")
	long countAllQuestions(@Param("receiverId") Long receiverId);

	@Query("SELECT q FROM MemberQuestion q " +
			"WHERE q.asker.id = :askerId AND q.receiver.id = :receiverId " +
			"AND q.answer IS NOT NULL " +
			"ORDER BY q.createdAt DESC, q.id DESC")
	List<MemberQuestion> findAllAnsweredByAskerAndReceiverOrderByLatest(
			@Param("askerId") Long askerId,
			@Param("receiverId") Long receiverId
	);

	@Query("SELECT q.id FROM MemberQuestion q " +
			"WHERE q.receiver.id = :receiverId " +
			"AND q.answer IS NOT NULL " +
			"ORDER BY q.createdAt DESC, q.id DESC")
	List<Long> findAllAnsweredQuestionIdsByReceiver(@Param("receiverId") Long receiverId);

	@Query("""
    SELECT q
    FROM MemberQuestion q
    WHERE q.receiver.id IN :receiverIds
      AND q.isReported = false
      AND q.createdAt >= :since
      AND NOT EXISTS (
          SELECT 1
          FROM MemberQuestion newer
          WHERE newer.receiver.id = q.receiver.id
            AND newer.isReported = false
            AND newer.createdAt >= :since
            AND (
                newer.createdAt > q.createdAt
                OR (newer.createdAt = q.createdAt AND newer.id > q.id)
            )
      )
    ORDER BY q.createdAt DESC, q.id DESC
""")
	List<MemberQuestion> findLatestRecentQuestionsByReceiverIds(
		@Param("receiverIds") List<Long> receiverIds,
		@Param("since") LocalDateTime since
	);

	// 답변완료 질문 위치 조회용 쿼리
	@Query("""
        SELECT COUNT(q)
        FROM MemberQuestion q
        WHERE q.receiver.id = :receiverId
          AND q.answer IS NOT NULL
          AND (
              q.createdAt > :createdAt
              OR (q.createdAt = :createdAt AND q.id > :questionId)
          )
    """)
	long countAnsweredQuestionsBeforeTargetInLatestOrder(
		@Param("receiverId") Long receiverId,
		@Param("createdAt") LocalDateTime createdAt,
		@Param("questionId") Long questionId
	);

	// 미답변 질문 위치 조회용 쿼리
	@Query("""
        SELECT COUNT(q)
        FROM MemberQuestion q
        WHERE q.receiver.id = :receiverId
          AND q.answer IS NULL
          AND (
              q.createdAt > :createdAt
              OR (q.createdAt = :createdAt AND q.id > :questionId)
          )
    """)
	long countUnansweredQuestionsBeforeTargetInLatestOrder(
		@Param("receiverId") Long receiverId,
		@Param("createdAt") LocalDateTime createdAt,
		@Param("questionId") Long questionId
	);

	@Query("""
    SELECT CASE WHEN COUNT(q) > 0 THEN true ELSE false END
    FROM MemberQuestion q
    WHERE q.receiver.id = :receiverId
      AND q.createdAt >= :since
""")
	boolean existsRecentQuestionByReceiver(
		@Param("receiverId") Long receiverId,
		@Param("since") LocalDateTime since
	);

	@Query("""
    SELECT q
    FROM MemberQuestion q
    WHERE q.isReported = false
      AND q.answer IS NOT NULL
    ORDER BY q.createdAt DESC, q.id DESC
""")
	List<MemberQuestion> findLatestAnsweredQuestions(Pageable pageable);

	// ------------------

	boolean existsByIdAndAsker(Long questionId, Member asker);

	boolean existsByIdAndReceiver(Long questionId, Member receiver);

	@Query("SELECT q FROM MemberQuestion q ORDER BY q.id DESC")
	List<MemberQuestion> findTopByOrderByIdDesc(Pageable pageable);

	@Query("SELECT q FROM MemberQuestion q WHERE q.receiver.id = :receiverId")
	List<MemberQuestion> findByReceiverId(@Param("receiverId") Long receiverId);
}