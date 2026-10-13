package org.sopt.makers.internal.sopmodam.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamCardType;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamPhase;

public record SopmodamCurrentCardResponse(
    Long roundId,
    String eventName,
    SopmodamPhase phase,
    SopmodamCardType cardType,
    LocalDateTime voteEndAt,
    LocalDateTime answerEndAt,
    // 소문자 한 글자 뒤에 대문자가 오는 이름이라 직렬화 이름을 고정한다
    @JsonProperty("dDay")
    Integer dDay,
    SopmodamQuestionResponse selectedQuestion,
    boolean canVote,
    boolean hasVoted,
    boolean canAnswer,
    boolean hasAnswered,
    Long answerCount
) {
}
