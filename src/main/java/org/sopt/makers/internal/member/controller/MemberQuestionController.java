package org.sopt.makers.internal.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.QuestionTab;
import org.sopt.makers.internal.member.dto.request.*;
import org.sopt.makers.internal.member.dto.response.*;
import org.sopt.makers.internal.member.service.MemberQuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Member Question 관련 API", description = "회원 질문/답변 관련 API List")
public class MemberQuestionController {

	private final MemberQuestionService memberQuestionService;

	@Operation(
		summary = "질문 작성 API",
		description = """
			다른 사용자에게 질문을 작성합니다.
			익명으로 작성할 경우 asker 정보가 숨겨집니다.
			"""
	)
	@PostMapping("/questions/{receiverId}")
	public ResponseEntity<Map<String, Long>> createQuestion(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
		@PathVariable Long receiverId,
		@RequestBody @Valid QuestionSaveRequest request
	) {
		Long questionId = memberQuestionService.createQuestion(userId, receiverId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("questionId", questionId));
	}

	@Operation(summary = "질문 수정 API", description = "답변이 달리기 전에만 수정 가능합니다.")
	@PutMapping("/questions/{questionId}")
	public ResponseEntity<Map<String, Boolean>> updateQuestion(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @PathVariable Long questionId,
		@RequestBody @Valid QuestionUpdateRequest request
	) {
		memberQuestionService.updateQuestion(userId, questionId, request);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
	}

	@Operation(
		summary = "질문 삭제 API",
		description = """
			질문 삭제 규칙:
			- 답변 전: 질문 작성자만 삭제 가능
			- 항상: 질문 받은 사람은 삭제 가능
			"""
	)
	@DeleteMapping("/questions/{questionId}")
	public ResponseEntity<Map<String, Boolean>> deleteQuestion(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @PathVariable Long questionId
	) {
		memberQuestionService.deleteQuestion(userId, questionId);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
	}

	@Operation(summary = "답변 작성 API", description = "질문을 받은 사람만 답변을 작성할 수 있습니다.")
	@PostMapping("/questions/{questionId}/answer")
	public ResponseEntity<Map<String, Long>> createAnswer(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @PathVariable Long questionId,
		@RequestBody @Valid AnswerSaveRequest request
	) {
		Long answerId = memberQuestionService.createAnswer(userId, questionId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("answerId", answerId));
	}

	@Operation(summary = "답변 수정 API", description = "답변 작성자만 수정 가능합니다.")
	@PutMapping("/answers/{answerId}")
	public ResponseEntity<Map<String, Boolean>> updateAnswer(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
		@PathVariable Long answerId,
		@RequestBody @Valid AnswerUpdateRequest request
	) {
		memberQuestionService.updateAnswer(userId, answerId, request);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
	}

	@Operation(summary = "답변 삭제 API", description = "답변 작성자만 삭제 가능합니다.")
	@DeleteMapping("/answers/{answerId}")
	public ResponseEntity<Map<String, Boolean>> deleteAnswer(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @PathVariable Long answerId
	) {
		memberQuestionService.deleteAnswer(userId, answerId);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
	}

	@Operation(
		summary = "나도 궁금해요 토글 API",
		description = """
			답변이 달리기 전 질문에 '나도 궁금해요' 반응을 토글합니다.
			이미 반응을 누른 경우 취소되고, 누르지 않은 경우 반응이 추가됩니다.
			질문을 받은 사람은 반응을 누를 수 없습니다.
			"""
	)
	@PostMapping("/questions/{questionId}/reactions")
	public ResponseEntity<Map<String, Boolean>> toggleQuestionReaction(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @PathVariable Long questionId
	) {
		memberQuestionService.toggleQuestionReaction(userId, questionId);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
	}

	@Operation(
		summary = "도움돼요 토글 API",
		description = """
			답변에 '도움돼요' 반응을 토글합니다.
			이미 반응을 누른 경우 취소되고, 누르지 않은 경우 반응이 추가됩니다.
			답변 작성자는 반응을 누를 수 없습니다.
			"""
	)
	@PostMapping("/answers/{answerId}/reactions")
	public ResponseEntity<Map<String, Boolean>> toggleAnswerReaction(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @PathVariable Long answerId
	) {
		memberQuestionService.toggleAnswerReaction(userId, answerId);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
	}

	@Operation(summary = "질문 신고 API")
	@PostMapping("/questions/{questionId}/report")
	public ResponseEntity<Map<String, Boolean>> reportQuestion(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @PathVariable Long questionId,
		@RequestBody QuestionReportRequest request
	) {
		memberQuestionService.reportQuestion(userId, questionId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true));
	}

	@Operation(
		summary = "질문 목록 조회 API",
		description = """
			특정 사용자의 질문 목록을 조회합니다.
			tab: answered (답변 완료), unanswered (새질문), 미입력 시 전체 조회
			page: 페이지 번호 (0부터 시작, 기본값 0)
			size: 페이지 크기 (기본 10, 최대 100)
			"""
	)
	@GetMapping("/{memberId}/questions")
	public ResponseEntity<QuestionsResponse> getQuestions(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @PathVariable Long memberId,
		@RequestParam(value = "tab", required = false) QuestionTab tab,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size
	) {
		QuestionsResponse response = memberQuestionService.getQuestions(userId, memberId, tab, page, size);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(
		summary = "답변 대기 중인 질문 개수 조회 API",
		description = "현재 로그인한 사용자에게 달린 답변 대기 중인 질문의 개수를 조회합니다."
	)
	@GetMapping("/me/questions/unanswered-count")
	public ResponseEntity<UnansweredCountResponse> getUnansweredCount(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId
	) {
		UnansweredCountResponse response = memberQuestionService.getUnansweredCount(userId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(
			summary = "내 질문의 답변 위치 조회 API",
			description = "특정 사용자의 답변 완료 탭에서 내가 남긴 가장 최신 질문이 몇 페이지 몇 번째에 있는지 조회합니다."
	)
	@GetMapping("/{memberId}/questions/my-latest-answered")
	public ResponseEntity<MyLatestAnsweredQuestionLocationResponse> getMyLatestAnsweredQuestionLocation(
			@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
			@PathVariable Long memberId
	) {
		MyLatestAnsweredQuestionLocationResponse response = memberQuestionService.getMyLatestAnsweredQuestionLocation(userId, memberId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
