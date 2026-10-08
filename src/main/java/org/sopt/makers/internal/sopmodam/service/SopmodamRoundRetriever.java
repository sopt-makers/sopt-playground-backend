package org.sopt.makers.internal.sopmodam.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.ConflictException;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.repository.SopmodamRoundRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SopmodamRoundRetriever {

    private final SopmodamRoundRepository roundRepository;

    public SopmodamRound findRoundById(Long roundId) {
        return roundRepository.findById(roundId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 솝모담 회차입니다."));
    }

    public List<SopmodamRound> findAllRounds() {
        return roundRepository.findAllByOrderByVoteStartAtDesc();
    }

    // 한 시점에는 회차 하나만 진행한다. 답변 공개 구간은 다음 회차가 시작되면 끝나므로 검사하지 않는다
    public void validateNotOverlapping(LocalDateTime voteStartAt, LocalDateTime answerEndAt) {
        if (roundRepository.existsOverlapping(voteStartAt, answerEndAt)) {
            throw new ConflictException("다른 회차와 일정이 겹칩니다.");
        }
    }

    public void validateNotOverlapping(Long roundId, LocalDateTime voteStartAt, LocalDateTime answerEndAt) {
        if (roundRepository.existsOverlappingExcept(roundId, voteStartAt, answerEndAt)) {
            throw new ConflictException("다른 회차와 일정이 겹칩니다.");
        }
    }
}
