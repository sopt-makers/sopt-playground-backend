package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberBlock;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.member.repository.MemberBlockRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberRetriever {

    private final MemberRepository memberRepository;
    private final MemberBlockRepository memberBlockRepository;

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자의 id값 입니다. id: [" + memberId + "]"));
    }

    public void checkExistsMemberById(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new NotFoundException("존재하지 않는 사용자의 id값 입니다. id: [" + memberId + "]");
        }
    }

    public void checkBlockedMember(Member blocker, Member blockedMember) {
        MemberBlock memberBlock = memberBlockRepository.findByBlockerAndBlockedMember(blocker, blockedMember)
                .orElseThrow(() -> new NotFoundException("차단되지 않은 사용자입니다."));
        if (Boolean.TRUE.equals(memberBlock.getIsBlocked())) {
            throw new BadRequestException("차단한 사용자의 게시글은 조회할 수 없습니다.");
        }
    }
}
