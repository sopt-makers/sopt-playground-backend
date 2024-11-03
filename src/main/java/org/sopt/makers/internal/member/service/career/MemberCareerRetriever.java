package org.sopt.makers.internal.member.service.career;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.MemberCareer;
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
}
