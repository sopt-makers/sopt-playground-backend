package org.sopt.makers.internal.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sopt.makers.internal.member.domain.AnswerReaction;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.domain.QuestionReaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

// users.work_preference 컬럼이 jsonb 라서, H2 에서 테이블을 만들 수 있도록 JSONB 도메인을 먼저 등록한다.
@DataJpaTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:member-reaction;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE DOMAIN IF NOT EXISTS JSONB AS JSON",
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = MemberReactionRepositoryTest.JpaConfig.class)
class MemberReactionRepositoryTest {

	@Configuration
	@EnableJpaAuditing
	@EntityScan(basePackages = "org.sopt.makers.internal")
	@EnableJpaRepositories(basePackageClasses = QuestionReactionRepository.class,
		excludeFilters = @Filter(type = FilterType.REGEX,
			pattern = ".*\\.repository\\.(?!(QuestionReaction|AnswerReaction)Repository$).*"))
	static class JpaConfig {
	}

	@Autowired TestEntityManager entityManager;
	@Autowired QuestionReactionRepository questionReactionRepository;
	@Autowired AnswerReactionRepository answerReactionRepository;

	@Test
	void 질문_ID_목록의_반응_수와_회원이_반응한_질문을_조회한다() {
		Member receiver = member(1L);
		Member me = member(2L);
		Member other = member(3L);
		MemberQuestion twoReactions = question(receiver);
		MemberQuestion oneReaction = question(receiver);
		MemberQuestion noReaction = question(receiver);
		MemberQuestion notRequested = question(receiver);

		entityManager.persist(QuestionReaction.builder().question(twoReactions).member(me).build());
		entityManager.persist(QuestionReaction.builder().question(twoReactions).member(other).build());
		entityManager.persist(QuestionReaction.builder().question(oneReaction).member(other).build());
		entityManager.persist(QuestionReaction.builder().question(notRequested).member(me).build());
		entityManager.flush();

		List<Long> questionIds = List.of(twoReactions.getId(), oneReaction.getId(), noReaction.getId());

		assertThat(questionReactionRepository.countByQuestionIds(questionIds))
			.extracting(
				QuestionReactionRepository.QuestionReactionCountProjection::getQuestionId,
				QuestionReactionRepository.QuestionReactionCountProjection::getReactionCount)
			.containsExactlyInAnyOrder(
				tuple(twoReactions.getId(), 2L),
				tuple(oneReaction.getId(), 1L));
		assertThat(questionReactionRepository.findReactedQuestionIds(me.getId(), questionIds))
			.containsExactly(twoReactions.getId());
	}

	@Test
	void 답변_ID_목록의_반응_수와_회원이_반응한_답변을_조회한다() {
		Member receiver = member(1L);
		Member me = member(2L);
		Member other = member(3L);
		MemberAnswer twoReactions = answer(question(receiver));
		MemberAnswer oneReaction = answer(question(receiver));
		MemberAnswer noReaction = answer(question(receiver));
		MemberAnswer notRequested = answer(question(receiver));

		entityManager.persist(AnswerReaction.builder().answer(twoReactions).member(me).build());
		entityManager.persist(AnswerReaction.builder().answer(twoReactions).member(other).build());
		entityManager.persist(AnswerReaction.builder().answer(oneReaction).member(other).build());
		entityManager.persist(AnswerReaction.builder().answer(notRequested).member(me).build());
		entityManager.flush();

		List<Long> answerIds = List.of(twoReactions.getId(), oneReaction.getId(), noReaction.getId());

		assertThat(answerReactionRepository.countByAnswerIds(answerIds))
			.extracting(
				AnswerReactionRepository.AnswerReactionCountProjection::getAnswerId,
				AnswerReactionRepository.AnswerReactionCountProjection::getReactionCount)
			.containsExactlyInAnyOrder(
				tuple(twoReactions.getId(), 2L),
				tuple(oneReaction.getId(), 1L));
		assertThat(answerReactionRepository.findReactedAnswerIds(me.getId(), answerIds))
			.containsExactly(twoReactions.getId());
	}

	private Member member(Long id) {
		return entityManager.persist(Member.builder().id(id).build());
	}

	private MemberQuestion question(Member receiver) {
		return entityManager.persist(MemberQuestion.builder()
			.receiver(receiver)
			.asker(receiver)
			.content("질문")
			.isAnonymous(false)
			.build());
	}

	private MemberAnswer answer(MemberQuestion question) {
		return entityManager.persist(MemberAnswer.builder()
			.question(question)
			.content("답변")
			.build());
	}
}
