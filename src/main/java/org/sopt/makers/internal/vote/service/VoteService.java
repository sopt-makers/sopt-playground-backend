package org.sopt.makers.internal.vote.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryGroup;
import org.sopt.makers.internal.community.service.post.CommunityPostRetriever;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.vote.domain.Vote;
import org.sopt.makers.internal.vote.domain.VoteOption;
import org.sopt.makers.internal.vote.domain.VoteSelection;
import org.sopt.makers.internal.vote.dto.request.VoteRequest;
import org.sopt.makers.internal.vote.dto.response.VoteOptionResponse;
import org.sopt.makers.internal.vote.dto.response.VoteResponse;
import org.sopt.makers.internal.vote.repository.VoteOptionRepository;
import org.sopt.makers.internal.vote.repository.VoteRepository;
import org.sopt.makers.internal.vote.repository.VoteSelectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteSelectionRepository voteSelectionRepository;

    private final CommunityPostRetriever communityPostRetriever;
    private final MemberRetriever memberRetriever;

    @Transactional
    public void createVote(CommunityPost post, VoteRequest voteRequest) {
        if (voteRequest == null) {
            return;
        }

        validateVotePolicy(post.getCategory(), voteRequest);

        Vote vote = Vote.of(post, voteRequest.isMultiple(), voteRequest.voteOptions());
        voteRepository.save(vote);
    }

    @Transactional
    public VoteResponse selectVote(Long postId, Long userId, List<Long> selectedOptionIds) {
        CommunityPost post = communityPostRetriever.findCommunityPostById(postId);
        Member member = memberRetriever.findMemberById(userId);
        Vote vote = voteRepository.findByPost(post)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 게시글에는 투표가 존재하지 않습니다."));

        List<VoteOption> selectedOptions = voteOptionRepository.findAllById(selectedOptionIds);

        // 이미 투표했으면 투표 불가
        if (voteSelectionRepository.existsByVoteOptionInAndMember(selectedOptions, member)) {
            throw new BadRequestException("이미 투표했습니다.");
        }

        validateVoteSelectionPolicy(vote, selectedOptionIds);

        for (VoteOption option : selectedOptions) {
            VoteSelection selection = VoteSelection.of(member, option);
            voteSelectionRepository.save(selection);
            option.increaseCount();
        }

        return getVoteByPostId(postId, userId);
    }

    @Transactional(readOnly = true)
    public VoteResponse getVoteByPostId(Long postId, Long userId) {
        return getVoteMapByPostIds(List.of(postId), userId).get(postId);
    }

    @Transactional(readOnly = true)
    public Map<Long, VoteResponse> getVoteMapByPostIds(List<Long> postIds, Long memberId) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctPostIds = postIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (distinctPostIds.isEmpty()) {
            return Map.of();
        }

        List<Vote> votes = voteRepository.findByPost_IdIn(distinctPostIds);

        if (votes.isEmpty()) {
            return Map.of();
        }

        List<Vote> distinctVotes = votes.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                Vote::getId,
                vote -> vote,
                (previous, current) -> previous
            ))
            .values()
            .stream()
            .toList();

        List<Long> voteIds = distinctVotes.stream()
            .map(Vote::getId)
            .toList();

        Map<Long, Integer> participantCountMap = voteSelectionRepository
            .countDistinctMembersByVoteIds(voteIds)
            .stream()
            .collect(Collectors.toMap(
                VoteSelectionRepository.VoteParticipantCountProjection::getVoteId,
                projection -> projection.getParticipantCount().intValue()
            ));

        Map<Long, Set<Long>> selectedOptionIdsByVoteId = memberId == null
            ? Map.of()
            : voteSelectionRepository.findSelectedOptionIdsByVoteIdsAndMemberId(voteIds, memberId)
              .stream()
              .collect(Collectors.groupingBy(
                  VoteSelectionRepository.MemberVoteSelectionProjection::getVoteId,
                  Collectors.mapping(
                      VoteSelectionRepository.MemberVoteSelectionProjection::getOptionId,
                      Collectors.toSet()
                  )
              ));

        Map<Long, VoteResponse> voteResponseMap = new HashMap<>();

        for (Vote vote : distinctVotes) {
            Long postId = vote.getPost().getId();
            Set<Long> selectedOptionIds = selectedOptionIdsByVoteId.getOrDefault(vote.getId(), Collections.emptySet());
            int participantCount = participantCountMap.getOrDefault(vote.getId(), 0);

            voteResponseMap.put(
                postId,
                toVoteResponse(vote, participantCount, selectedOptionIds)
            );
        }

        return voteResponseMap;
    }

    private VoteResponse toVoteResponse(
        Vote vote,
        int totalParticipants,
        Set<Long> selectedOptionIds
    ) {
        List<VoteOption> sortedOptions = vote.getVoteOptions().stream()
            .sorted(Comparator.comparing(VoteOption::getId))
            .toList();

        boolean hasVoted = selectedOptionIds != null && !selectedOptionIds.isEmpty();

        int totalVoteCount = sortedOptions.stream()
            .mapToInt(VoteOption::getVoteCount)
            .sum();

        List<VoteOptionResponse> optionsResponse = sortedOptions.stream()
            .map(option -> new VoteOptionResponse(
                option.getId(),
                option.getContent(),
                option.getVoteCount(),
                calculateVotePercent(option.getVoteCount(), totalVoteCount),
                selectedOptionIds != null && selectedOptionIds.contains(option.getId())
            ))
            .toList();

        return new VoteResponse(
            vote.getId(),
            vote.isMultipleOptions(),
            hasVoted,
            totalParticipants,
            optionsResponse
        );
    }

    private int calculateVotePercent(int voteCount, int totalCount) {
        if (totalCount == 0) {
            return 0;
        }

        return (int) Math.round((double) voteCount * 100 / totalCount);
    }

    private void validateVotePolicy(Category category, VoteRequest vote) {
        if (category != null && category.getCategoryGroup() == CommunityCategoryGroup.SOPTICLE) {
            throw new BadRequestException("솝티클 카테고리는 투표를 만들 수 없습니다.");
        }

        List<String> options = vote.voteOptions();

        if (options == null || options.size() < 2 || options.size() > 5) {
            throw new BadRequestException("투표 옵션은 2개 이상 5개 이하만 가능합니다.");
        }

        for (String option : options) {
            if (option == null || option.trim().isEmpty()) {
                throw new BadRequestException("투표 옵션 내용은 공백일 수 없습니다.");
            }

            if (option.length() > 40) {
                throw new BadRequestException("투표 옵션은 40자까지만 입력 가능합니다.");
            }
        }
    }

    private void validateVoteSelectionPolicy(Vote vote, List<Long> selectedOptionIds) {
        // 복수 투표 불가인 경우 옵션 2개 이상 불가능
        if (!vote.isMultipleOptions() && selectedOptionIds.size() > 1) {
            throw new BadRequestException("복수 선택 불가능한 투표입니다.");
        }

        // 선택한 옵션이 존재하지 않는 경우 체크
        List<VoteOption> selectedOptions = voteOptionRepository.findAllById(selectedOptionIds);

        if (selectedOptions.size() != selectedOptionIds.size()) {
            throw new BadRequestException("존재하지 않는 투표 옵션이 포함되어 있습니다.");
        }
    }
}
