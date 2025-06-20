package org.sopt.makers.internal.vote.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.vote.domain.Vote;
import org.sopt.makers.internal.vote.dto.request.VoteRequest;
import org.sopt.makers.internal.vote.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;

    @Transactional
    public void createVote(CommunityPost post, VoteRequest voteRequest) {
        validateVotePolicy(post.getCategoryId(), voteRequest);

        Vote vote = Vote.of(post, voteRequest.isMultiple(), voteRequest.voteOptions());
        voteRepository.save(vote);
    }

    private void validateVotePolicy(Long categoryId, VoteRequest vote) {
        if (Objects.isNull(vote)) return;

        if (Objects.equals(categoryId, 21L)) {
            throw new ClientBadRequestException("솝티클 카테고리는 투표를 만들 수 없습니다.");
        }

        List<String> options = vote.voteOptions();
        if (options == null || options.size() < 2 || options.size() > 5) {
            throw new ClientBadRequestException("투표 옵션은 2개 이상 5개 이하만 가능합니다.");
        }

        for (String option : options) {
            if (option == null || option.trim().isEmpty()) {
                throw new ClientBadRequestException("투표 옵션 내용은 공백일 수 없습니다.");
            }
            if (option.length() > 40) {
                throw new ClientBadRequestException("투표 옵션은 40자까지만 입력 가능합니다.");
            }
        }
    }
}
