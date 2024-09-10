package org.sopt.makers.internal.service;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberRepository;

public class MemberServiceUtil {

    public static Member findMemberById(MemberRepository memberRepository, Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("존재하지 않는 사용자의 id값 입니다."));
    }

    public static void checkExistsMemberById(MemberRepository memberRepository, Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new NotFoundDBEntityException("존재하지 않는 사용자의 id값 입니다.");
        }
    }
}
