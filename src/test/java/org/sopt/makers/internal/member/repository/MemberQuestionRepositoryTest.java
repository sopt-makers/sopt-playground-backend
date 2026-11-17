package org.sopt.makers.internal.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import jakarta.persistence.EntityManagerFactory;

// users.work_preference 컬럼이 jsonb 라서, H2 에서 테이블을 만들 수 있도록 JSONB 도메인을 먼저 등록한다.
@DataJpaTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:member-question;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE DOMAIN IF NOT EXISTS JSONB AS JSON",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.jpa.properties.hibernate.generate_statistics=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = MemberQuestionRepositoryTest.JpaConfig.class)
class MemberQuestionRepositoryTest {

	@Configuration
	@EnableJpaAuditing
	@EntityScan(basePackages = "org.sopt.makers.internal")
	@EnableJpaRepositories(basePackageClasses = MemberQuestionRepository.class,
		excludeFilters = @Filter(type = FilterType.REGEX, pattern = ".*\\.repository\\.(?!MemberQuestionRepository$).*"))
	static class JpaConfig {
	}

	@Autowired TestEntityManager entityManager;
	@Autowired EntityManagerFactory entityManagerFactory;
	@Autowired MemberQuestionRepository memberQuestionRepository;

	private Long receiverId;

	@BeforeEach
	void setUp() {
		Member receiver = entityManager.persist(Member.builder().id(1L).build());
		Member asker = entityManager.persist(Member.builder().id(2L).build());
		receiverId = receiver.getId();

		AnonymousNickname nickname = entityManager.persist(AnonymousNickname.builder().nickname("익명1").build());
		AnonymousProfileImage profileImage = entityManager.persist(
			AnonymousProfileImage.builder().imageUrl("image").build());

		MemberQuestion answeredAnonymous = question(receiver, asker, true, nickname, profileImage);
		MemberQuestion answered = question(receiver, asker, false, null, null);
		question(receiver, asker, true, nickname, profileImage);
		question(receiver, asker, false, null, null);

		answer(answeredAnonymous);
		answer(answered);

		entityManager.flush();
		entityManager.clear();
	}

	@Test
	void 전체_질문_목록은_답변과_익명_프로필을_한_번의_쿼리로_조회한다() {
		Statistics statistics = startStatistics();

		List<MemberQuestion> questions = memberQuestionRepository.findAllQuestions(receiverId, PageRequest.of(0, 10));
		touchAssociations(questions);

		assertThat(questions).hasSize(4);
		assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
	}

	@Test
	void 답변_완료_질문_목록은_답변과_익명_프로필을_한_번의_쿼리로_조회한다() {
		Statistics statistics = startStatistics();

		List<MemberQuestion> questions = memberQuestionRepository.findAnsweredQuestions(receiverId, PageRequest.of(0, 10));
		touchAssociations(questions);

		assertThat(questions).hasSize(2).allMatch(MemberQuestion::hasAnswer);
		assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
	}

	@Test
	void 미답변_질문_목록은_답변과_익명_프로필을_한_번의_쿼리로_조회한다() {
		Statistics statistics = startStatistics();

		List<MemberQuestion> questions = memberQuestionRepository.findUnansweredQuestions(receiverId, PageRequest.of(0, 10));
		touchAssociations(questions);

		assertThat(questions).hasSize(2).noneMatch(MemberQuestion::hasAnswer);
		assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
	}

	private Statistics startStatistics() {
		Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
		statistics.clear();
		return statistics;
	}

	private void touchAssociations(List<MemberQuestion> questions) {
		for (MemberQuestion question : questions) {
			if (question.getAnswer() != null) {
				question.getAnswer().getContent();
			}
			if (question.getAnonymousNickname() != null) {
				question.getAnonymousNickname().getNickname();
			}
			if (question.getAnonymousProfileImage() != null) {
				question.getAnonymousProfileImage().getImageUrl();
			}
		}
	}

	private MemberQuestion question(
		Member receiver,
		Member asker,
		boolean isAnonymous,
		AnonymousNickname nickname,
		AnonymousProfileImage profileImage
	) {
		return entityManager.persist(MemberQuestion.builder()
			.receiver(receiver)
			.asker(asker)
			.content("질문")
			.isAnonymous(isAnonymous)
			.anonymousNickname(nickname)
			.anonymousProfileImage(profileImage)
			.build());
	}

	private void answer(MemberQuestion question) {
		entityManager.persist(MemberAnswer.builder()
			.question(question)
			.content("답변")
			.build());
	}
}
