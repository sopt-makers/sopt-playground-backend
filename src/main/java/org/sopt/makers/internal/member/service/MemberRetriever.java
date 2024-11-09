package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.member.repository.soptactivity.MemberSoptActivityRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberRetriever {

    private final MemberRepository memberRepository;
    private final MemberSoptActivityRepository memberSoptActivityRepository;

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("존재하지 않는 사용자의 id값 입니다. id: [" + memberId + "]"));
    }

    public void checkExistsMemberById(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new NotFoundDBEntityException("존재하지 않는 사용자의 id값 입니다. id: [" + memberId + "]");
        }
    }

    // TODO YB/OB 회원에 대한 활동 정보 Validation 추가
    public List<String> concatPartAndGeneration(Long memberId) {
        return memberSoptActivityRepository.findAllByMemberId(memberId).stream()
                .map(activity -> String.format("%d기 %s", activity.getGeneration(), activity.getPart()))
                .toList();
    }
}
