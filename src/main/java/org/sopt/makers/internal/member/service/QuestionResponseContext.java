package org.sopt.makers.internal.member.service;

import java.util.Map;
import java.util.Set;

import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;

record QuestionResponseContext(
	Map<Long, InternalUserDetails> userDetailsMap,
	Map<Long, Long> questionReactionCounts,
	Set<Long> reactedQuestionIds,
	Map<Long, Long> answerReactionCounts,
	Set<Long> reactedAnswerIds
) {

	static QuestionResponseContext empty() {
		return new QuestionResponseContext(Map.of(), Map.of(), Set.of(), Map.of(), Set.of());
	}

	// 반응이 없는 질문·답변은 집계 결과에 없으므로 0, false 로 응답한다.
	Long getQuestionReactionCount(Long questionId) {
		return questionReactionCounts.getOrDefault(questionId, 0L);
	}

	Boolean isQuestionReacted(Long questionId) {
		return reactedQuestionIds.contains(questionId);
	}

	Long getAnswerReactionCount(Long answerId) {
		return answerReactionCounts.getOrDefault(answerId, 0L);
	}

	Boolean isAnswerReacted(Long answerId) {
		return reactedAnswerIds.contains(answerId);
	}

	// 기존 단건 조회 실패와 동일하게, 플랫폼 정보가 없으면 질문 목록 조회 전체를 실패시킨다.
	InternalUserDetails getUserDetails(Long userId) {
		InternalUserDetails userDetails = userDetailsMap.get(userId);
		if (userDetails == null) {
			throw new NotFoundException("[INTERNAL-404] 플랫폼에 해당 유저 정보가 없습니다. 요청 ID: " + userId);
		}
		return userDetails;
	}
}
