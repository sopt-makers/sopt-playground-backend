package org.sopt.makers.internal.member.dto.profile;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 프로필 요약 VO 의 "채움 여부" 판정 테스트.
 *
 * <p>이 판정이 리팩터링 전 가중치 전략의 {@code (값 != null && !값.isBlank())} 와
 * 같은 의미여야 정렬 순서가 보존된다. 특히 아래 두 가지를 혼동하면 가중치가 틀어진다.
 * <ul>
 *   <li>"값이 있음" vs "값이 참" — {@code false} 도 응답한 값이다</li>
 *   <li>"값이 있음" vs "값이 0 이 아님" — {@code 0.0} 도 응답한 값이다</li>
 * </ul>
 */
class MemberProfileSummaryVoTest {

	@Nested
	@DisplayName("MemberBasicInfoVo")
	class BasicInfo {

		@Test
		@DisplayName("세 필드가 모두 채워지면 3을 반환한다")
		void 세_필드가_모두_채워지면_3() {
			MemberBasicInfoVo vo = new MemberBasicInfoVo("서울시 강남구", "서울대학교", "컴퓨터공학과");
			assertThat(vo.filledCount()).isEqualTo(3);
		}

		@Test
		@DisplayName("null 인 필드는 세지 않는다")
		void null_필드는_제외() {
			MemberBasicInfoVo vo = new MemberBasicInfoVo(null, "서울대학교", null);
			assertThat(vo.filledCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("공백만 있는 문자열은 채워진 것으로 보지 않는다")
		void 공백_문자열은_제외() {
			MemberBasicInfoVo vo = new MemberBasicInfoVo("   ", "", "\t\n");
			assertThat(vo.filledCount()).isZero();
		}

		@Test
		@DisplayName("대학교명 부분 일치를 판정한다")
		void 대학교_부분일치() {
			MemberBasicInfoVo vo = new MemberBasicInfoVo(null, "서울대학교", null);
			assertThat(vo.matchesUniversity("서울")).isTrue();
			assertThat(vo.matchesUniversity("연세")).isFalse();
		}

		@Test
		@DisplayName("대학교가 null 이면 매칭되지 않는다")
		void 대학교_null이면_매칭안됨() {
			MemberBasicInfoVo vo = new MemberBasicInfoVo(null, null, null);
			assertThat(vo.matchesUniversity("서울")).isFalse();
		}
	}

	@Nested
	@DisplayName("MemberIntroVo")
	class Intro {

		@Test
		@DisplayName("정상 값이면 true 를 반환한다")
		void 정상값() {
			MemberIntroVo vo = new MemberIntroVo("소개", "자기소개", "Java");
			assertThat(vo.hasIntroduction()).isTrue();
			assertThat(vo.hasSelfIntroduction()).isTrue();
			assertThat(vo.hasSkill()).isTrue();
		}

		@Test
		@DisplayName("null·빈문자열·공백은 모두 false 를 반환한다")
		void null과_빈문자열과_공백() {
			assertThat(new MemberIntroVo(null, null, null).hasIntroduction()).isFalse();
			assertThat(new MemberIntroVo("", null, null).hasIntroduction()).isFalse();
			assertThat(new MemberIntroVo("   ", null, null).hasIntroduction()).isFalse();
		}
	}

	@Nested
	@DisplayName("MemberPersonalityVo")
	class Personality {

		@Test
		@DisplayName("문자열 4개와 숫자 1개가 모두 채워지면 5를 반환한다")
		void 모두_채워지면_5() {
			MemberPersonalityVo vo = new MemberPersonalityVo("INTJ", "설명", 1.5, "관심사", "이상형");
			assertThat(vo.filledCount()).isEqualTo(5);
		}

		@Test
		@DisplayName("sojuCapacity 는 null 여부만 본다 — 0.0 도 응답한 값이다")
		void 주량_0점0도_카운트() {
			MemberPersonalityVo vo = new MemberPersonalityVo(null, null, 0.0, null, null);
			assertThat(vo.filledCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("sojuCapacity 가 null 이면 세지 않는다")
		void 주량_null은_제외() {
			MemberPersonalityVo vo = new MemberPersonalityVo(null, null, null, null, null);
			assertThat(vo.filledCount()).isZero();
		}

		@Test
		@DisplayName("문자열 필드의 공백은 채워진 것으로 보지 않는다")
		void 문자열_공백은_제외() {
			MemberPersonalityVo vo = new MemberPersonalityVo("  ", "", 1.0, "  ", "");
			assertThat(vo.filledCount()).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("MemberFavorVo")
	class Favor {

		@Test
		@DisplayName("6개가 전부 null 이면 0을 반환한다 — 엔티티의 userFavor == null 과 동치")
		void 전부_null이면_0() {
			MemberFavorVo vo = new MemberFavorVo(null, null, null, null, null, null);
			assertThat(vo.answeredCount()).isZero();
		}

		@Test
		@DisplayName("false 도 응답한 값이므로 센다")
		void false도_카운트() {
			MemberFavorVo vo = new MemberFavorVo(false, false, false, null, null, null);
			assertThat(vo.answeredCount()).isEqualTo(3);
		}

		@Test
		@DisplayName("true 와 false 가 섞여도 응답한 개수를 센다")
		void 혼합() {
			MemberFavorVo vo = new MemberFavorVo(true, false, true, null, false, null);
			assertThat(vo.answeredCount()).isEqualTo(4);
		}
	}

	@Nested
	@DisplayName("MemberActivityCountVo")
	class ActivityCount {

		@Test
		@DisplayName("회사명 부분 일치를 판정한다")
		void 회사명_부분일치() {
			MemberActivityCountVo vo = new MemberActivityCountVo(2, 2, List.of("토스", "카카오"));
			assertThat(vo.matchesCompany("토스")).isTrue();
			assertThat(vo.matchesCompany("카카")).isTrue();
			assertThat(vo.matchesCompany("네이버")).isFalse();
		}

		@Test
		@DisplayName("회사명이 비어 있으면 매칭되지 않는다")
		void 빈_회사명() {
			assertThat(MemberActivityCountVo.EMPTY.matchesCompany("토스")).isFalse();
		}

		@Test
		@DisplayName("companyNames 에 null 을 넘겨도 빈 리스트로 방어한다")
		void null_리스트_방어() {
			MemberActivityCountVo vo = new MemberActivityCountVo(0, 0, null);
			assertThat(vo.companyNames()).isEmpty();
			assertThat(vo.matchesCompany("토스")).isFalse();
		}

		@Test
		@DisplayName("careerCount 와 companyNames 크기는 다를 수 있다 — company_name 이 NULL 인 커리어")
		void careerCount와_회사명_개수는_다를_수_있다() {
			MemberActivityCountVo vo = new MemberActivityCountVo(0, 3, List.of("토스"));
			assertThat(vo.careerCount()).isEqualTo(3);
			assertThat(vo.companyNames()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("MemberProfileSummaryVo")
	class Summary {

		@Test
		@DisplayName("대학교 또는 회사명 중 하나만 맞아도 true 를 반환한다")
		void 대학교_또는_회사명() {
			MemberProfileSummaryVo onlyUniversity = summary("서울대학교", List.of());
			MemberProfileSummaryVo onlyCompany = summary(null, List.of("토스"));
			MemberProfileSummaryVo neither = summary("연세대학교", List.of("카카오"));

			assertThat(onlyUniversity.matchesUniversityOrCompany("서울")).isTrue();
			assertThat(onlyCompany.matchesUniversityOrCompany("토스")).isTrue();
			assertThat(neither.matchesUniversityOrCompany("서울")).isFalse();
		}

		@Test
		@DisplayName("5-arg 생성자로 만들면 activity 가 EMPTY 로 초기화된다")
		void 프로젝션용_생성자() {
			MemberProfileSummaryVo vo = new MemberProfileSummaryVo(
				1L,
				new MemberBasicInfoVo(null, null, null),
				new MemberIntroVo(null, null, null),
				new MemberPersonalityVo(null, null, null, null, null),
				new MemberFavorVo(null, null, null, null, null, null)
			);
			assertThat(vo.activity()).isSameAs(MemberActivityCountVo.EMPTY);
		}

		@Test
		@DisplayName("withAggregates 는 집계값만 교체하고 나머지는 유지한다")
		void withAggregates() {
			MemberProfileSummaryVo base = summary("서울대학교", List.of());
			MemberProfileSummaryVo result = base.withActivity(new MemberActivityCountVo(2, 1, List.of("토스")));

			assertThat(result.id()).isEqualTo(base.id());
			assertThat(result.basicInfo()).isEqualTo(base.basicInfo());
			assertThat(result.activity().linkCount()).isEqualTo(2);
			assertThat(result.activity().careerCount()).isEqualTo(1);
		}

		private MemberProfileSummaryVo summary(String university, List<String> companyNames) {
			return new MemberProfileSummaryVo(
				1L,
				new MemberBasicInfoVo(null, university, null),
				new MemberIntroVo(null, null, null),
				new MemberPersonalityVo(null, null, null, null, null),
				new MemberFavorVo(null, null, null, null, null, null),
				new MemberActivityCountVo(0, companyNames.size(), companyNames)
			);
		}
	}
}
