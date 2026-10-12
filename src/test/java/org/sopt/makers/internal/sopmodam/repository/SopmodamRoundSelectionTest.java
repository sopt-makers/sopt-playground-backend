package org.sopt.makers.internal.sopmodam.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ContextConfiguration(classes = SopmodamRoundSelectionTest.JpaConfig.class)
class SopmodamRoundSelectionTest {

    @Configuration
    @EnableJpaAuditing
    @EnableJpaRepositories(basePackageClasses = SopmodamRoundRepository.class,
        excludeFilters = @Filter(type = FilterType.REGEX, pattern = ".*Sopmodam(Answer|QuestionVote)Repository"))
    static class JpaConfig {
        @Bean
        PersistenceManagedTypes managedTypes() {
            return PersistenceManagedTypes.of(SopmodamRound.class.getName(), SopmodamQuestion.class.getName());
        }
    }

    @Autowired TestEntityManager entityManager;
    @Autowired SopmodamRoundRepository roundRepository;

    @Test
    void 조건부_저장은_기존_선정값을_덮어쓰지_않는다() {
        SopmodamRound round = round(LocalDateTime.now().minusHours(1));
        SopmodamQuestion first = question(round, 5);
        SopmodamQuestion second = question(round, 3);
        Long roundId = round.getId();
        Long firstId = first.getId();
        Long secondId = second.getId();

        assertThat(roundRepository.selectQuestionIfAbsent(roundId, firstId)).isEqualTo(1);
        assertThat(roundRepository.selectQuestionIfAbsent(roundId, secondId)).isZero();
        assertThat(roundRepository.findById(roundId).orElseThrow().getSelectedQuestionId()).isEqualTo(firstId);
    }

    private SopmodamRound round(LocalDateTime voteEndAt) {
        return entityManager.persistAndFlush(SopmodamRound.builder()
            .generation(37).eventName("회차")
            .voteStartAt(voteEndAt.minusDays(1)).voteEndAt(voteEndAt)
            .answerEndAt(voteEndAt.plusDays(1)).build());
    }

    private SopmodamQuestion question(SopmodamRound round, int voteCount) {
        return entityManager.persistAndFlush(SopmodamQuestion.builder()
            .round(round).content("질문").voteCount(voteCount).build());
    }
}
