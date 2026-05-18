package org.sopt.makers.internal.member.service.career;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.member.repository.career.MemberCareerRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberCareerRetriever {

    private final MemberCareerRepository memberCareerRepository;

    public MemberCareer findMemberLastCareerByMemberId(Long memberId) {
        return memberCareerRepository.findMemberLastCareerByMemberId(memberId)
                .orElse(null);
    }

    public Map<Long, MemberCareer> findMemberLastCareerMapByMemberIds(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctMemberIds = memberIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (distinctMemberIds.isEmpty()) {
            return Map.of();
        }

        List<MemberCareer> careers = memberCareerRepository.findMemberLastCareersByMemberIds(distinctMemberIds);

        Map<Long, MemberCareer> careerMap = new LinkedHashMap<>();

        for (MemberCareer career : careers) {
            careerMap.putIfAbsent(career.getMemberId(), career);
        }

        return careerMap;
    }
}
