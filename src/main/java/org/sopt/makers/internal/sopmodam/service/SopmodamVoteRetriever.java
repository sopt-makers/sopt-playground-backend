package org.sopt.makers.internal.sopmodam.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.ConflictException;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionVoteRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SopmodamVoteRetriever {

    private final SopmodamQuestionVoteRepository questionVoteRepository;

    public Optional<Long> findVotedQuestionId(Long roundId, Long memberId) {
        return questionVoteRepository.findQuestionIdByRoundIdAndMemberId(roundId, memberId);
    }

    public void validateNotVoted(Long roundId, Long memberId) {
        if (questionVoteRepository.existsByRoundIdAndMemberId(roundId, memberId)) {
            throw new ConflictException("이미 투표한 회차입니다.");
        }
    }
}
