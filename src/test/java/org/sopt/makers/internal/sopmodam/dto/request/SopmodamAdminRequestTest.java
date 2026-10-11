package org.sopt.makers.internal.sopmodam.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 어드민 요청의 앞뒤 공백 제거 테스트.
 *
 * <p>어드민 화면처럼 Swagger 로 직접 호출해도 앞뒤 공백을 제거한 값으로 검증·저장해야 한다.
 */
class SopmodamAdminRequestTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private final LocalDateTime voteStartAt = LocalDateTime.of(2026, 10, 10, 0, 0);
	private final LocalDateTime voteEndAt = LocalDateTime.of(2026, 10, 15, 0, 0);
	private final LocalDateTime answerEndAt = LocalDateTime.of(2026, 11, 5, 0, 0);

	@Test
	@DisplayName("질문 추가 요청은 앞뒤 공백을 제거한다")
	void 질문_추가_앞뒤_공백_제거() {
		SopmodamQuestionCreateRequest request = new SopmodamQuestionCreateRequest("  새 질문\n");

		assertThat(request.content()).isEqualTo("새 질문");
		assertThat(validator.validate(request)).isEmpty();
	}

	@Test
	@DisplayName("질문 추가 요청은 공백만 있으면 검증에 실패한다")
	void 질문_추가_공백만_있으면_실패() {
		assertThat(validator.validate(new SopmodamQuestionCreateRequest("   ")))
			.extracting(ConstraintViolation::getMessage)
			.containsExactly("필요한 값이 없습니다");
	}

	@Test
	@DisplayName("회차 생성 요청은 회차 주제와 각 질문의 앞뒤 공백을 제거한다")
	void 회차_생성_앞뒤_공백_제거() {
		SopmodamRoundCreateRequest request = new SopmodamRoundCreateRequest(
			37, " SOPT 첫 협업 ", voteStartAt, voteEndAt, answerEndAt, List.of(" 질문 1", "질문 2 ")
		);

		assertThat(request.eventName()).isEqualTo("SOPT 첫 협업");
		assertThat(request.questions()).containsExactly("질문 1", "질문 2");
	}

	@Test
	@DisplayName("회차 수정 요청은 회차 주제의 앞뒤 공백을 제거한다")
	void 회차_수정_앞뒤_공백_제거() {
		SopmodamRoundUpdateRequest request = new SopmodamRoundUpdateRequest(
			37, " SOPT 첫 협업 ", voteStartAt, voteEndAt, answerEndAt
		);

		assertThat(request.eventName()).isEqualTo("SOPT 첫 협업");
	}
}
