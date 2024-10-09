package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberRetriever {

    private final MemberRepository memberRepository;

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("존재하지 않는 사용자의 id값 입니다. id: [" + memberId + "]"));
    }
}
