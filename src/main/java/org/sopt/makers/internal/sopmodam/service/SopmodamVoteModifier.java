package org.sopt.makers.internal.sopmodam.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.common.util.ConstraintViolationUtil;
import org.sopt.makers.internal.exception.ConflictException;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestionVote;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionRepository;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionVoteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SopmodamVoteModifier {

    private static final String VOTE_ROUND_MEMBER_UNIQUE_CONSTRAINT = "uk_sopmodam_question_vote_round_member";

    private final SopmodamQuestionVoteRepository questionVoteRepository;
    private final SopmodamQuestionRepository questionRepository;

    // 투표 행 INSERT 와 vote_count 증가를 한 트랜잭션으로 묶는다.
    // Platform 호출을 트랜잭션 밖에 두기 위해 서비스가 아니라 여기서 트랜잭션 경계를 잡는다.
    @Transactional
    public SopmodamQuestionVote createVote(SopmodamRound round, SopmodamQuestion question, Member member) {
        SopmodamQuestionVote vote = SopmodamQuestionVote.builder()
            .round(round)
            .question(question)
            .member(member)
            .build();

        // 동시 요청으로 (round_id, member_id) 유니크 제약에 걸리면 커밋 전에 여기서 409 로 바꾼다
        try {
            questionVoteRepository.saveAndFlush(vote);
        } catch (DataIntegrityViolationException exception) {
            if (ConstraintViolationUtil.isViolated(exception, VOTE_ROUND_MEMBER_UNIQUE_CONSTRAINT)) {
                throw new ConflictException("이미 투표한 회차입니다.");
            }
            throw exception;
        }

        questionRepository.increaseVoteCount(question.getId());
        return vote;
    }
}
