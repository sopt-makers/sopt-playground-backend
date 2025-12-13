package org.sopt.makers.internal.member.service.workpreference;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.WorkPreference;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.sopt.makers.internal.common.Constant.CURRENT_GENERATION;

@Component
@RequiredArgsConstructor
public class WorkPreferenceRetriever {

    private final MemberRepository memberRepository;
    private final MemberRetriever memberRetriever;
    private final PlatformService platformService;

    public WorkPreference getWorkPreferenceByMemberId(Long memberId) {
        Member member = memberRetriever.findMemberById(memberId);
        return member.getWorkPreference();
    }

    public void validateWorkPreferenceExists(Long memberId) {
        WorkPreference workPreference = getWorkPreferenceByMemberId(memberId);
        if (workPreference == null) {
            throw new ClientBadRequestException("작업 성향이 설정되지 않았습니다. 먼저 작업 성향을 설정해주세요.");
        }
    }

    public void validateCurrentGeneration(Long userId) {
        InternalUserDetails userDetails = platformService.getInternalUser(userId);
        if (userDetails.lastGeneration() != CURRENT_GENERATION) {
            throw new ClientBadRequestException("최신 기수만 조회가 가능합니다.");
        }
    }

    public List<Member> findMembersWithWorkPreference(Long excludeUserId) {
        return memberRepository.findAllByWorkPreferenceNotNull().stream()
                .filter(member -> !member.getId().equals(excludeUserId))
                .toList();
    }

    public Map<Long, InternalUserDetails> getLatestGenerationMembersMap(List<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<InternalUserDetails> userDetailsList = platformService.getInternalUsers(memberIds);

        return userDetailsList.stream()
                .filter(user -> user.lastGeneration() == CURRENT_GENERATION)
                .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));
    }

    public List<Member> filterCandidatesByMatchCount(List<Member> candidates, WorkPreference currentPreference, int minMatchCount) {
        return candidates.stream()
                .filter(member -> {
                    WorkPreference candidatePreference = member.getWorkPreference();
                    int matchCount = calculateMatchCount(currentPreference, candidatePreference);
                    return matchCount >= minMatchCount;
                })
                .toList();
    }

    public int calculateMatchCount(WorkPreference current, WorkPreference candidate) {
        int count = 0;
        if (current.getIdeationStyle() == candidate.getIdeationStyle()) count++;
        if (current.getWorkTime() == candidate.getWorkTime()) count++;
        if (current.getCommunicationStyle() == candidate.getCommunicationStyle()) count++;
        if (current.getWorkPlace() == candidate.getWorkPlace()) count++;
        if (current.getFeedbackStyle() == candidate.getFeedbackStyle()) count++;
        return count;
    }
}