package org.sopt.makers.internal.sopmodam.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionRepository;
import org.sopt.makers.internal.sopmodam.repository.SopmodamRoundRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SopmodamRoundModifier {

    private final SopmodamRoundRepository roundRepository;
    private final SopmodamQuestionRepository questionRepository;

    // 투표 종료 후 단독 최다 득표만 자동 선정한다. 기존 선정값을 덮어쓰지 않고 실제 저장값을 다시 읽는다
    @Transactional
    public SopmodamRound selectFinalQuestionIfAbsent(SopmodamRound round, LocalDateTime now) {
        if (now.isBefore(round.getVoteEndAt()) || round.getSelectedQuestionId() != null) {
            return round;
        }

        List<SopmodamQuestion> questions = questionRepository.findAllByRoundIdOrderByIdAsc(round.getId());
        int maxVotes = questions.stream().mapToInt(SopmodamQuestion::getVoteCount).max().orElse(0);
        List<SopmodamQuestion> topQuestions = questions.stream()
            .filter(question -> question.getVoteCount() == maxVotes)
            .toList();
        if (topQuestions.size() == 1) {
            roundRepository.selectQuestionIfAbsent(round.getId(), topQuestions.get(0).getId());
        }

        return roundRepository.findById(round.getId())
            .orElseThrow(() -> new NotFoundException("존재하지 않는 솝모담 회차입니다."));
    }

    public SopmodamRound createRound(
        Integer generation,
        String eventName,
        LocalDateTime voteStartAt,
        LocalDateTime voteEndAt,
        LocalDateTime answerEndAt
    ) {
        return roundRepository.save(SopmodamRound.builder()
            .generation(generation)
            .eventName(eventName)
            .voteStartAt(voteStartAt)
            .voteEndAt(voteEndAt)
            .answerEndAt(answerEndAt)
            .build());
    }

    public void updateRound(
        SopmodamRound round,
        Integer generation,
        String eventName,
        LocalDateTime voteStartAt,
        LocalDateTime voteEndAt,
        LocalDateTime answerEndAt
    ) {
        round.updateRound(generation, eventName, voteStartAt, voteEndAt, answerEndAt);
        roundRepository.save(round);
    }
}
