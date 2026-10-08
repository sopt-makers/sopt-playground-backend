package org.sopt.makers.internal.sopmodam.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SopmodamQuestionListResponse(
    Long roundId,
    String eventName,
    LocalDateTime voteEndAt,
    // 소문자 한 글자 뒤에 대문자가 오는 이름이라 직렬화 이름을 고정한다
    @JsonProperty("dDay")
    Integer dDay,
    boolean canVote,
    boolean hasVoted,
    Long myVoteQuestionId,
    List<SopmodamQuestionResponse> questions
) {
}
