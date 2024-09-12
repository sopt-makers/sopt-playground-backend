package org.sopt.makers.internal.service.member;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.member.MemberBlock;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.member.MemberBlockRepository;

public class MemberServiceUtil {

    public static Member findMemberById(MemberRepository memberRepository, Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("존재하지 않는 사용자의 id값 입니다. id: [" + memberId + "]"));
    }

    public static void checkExistsMemberById(MemberRepository memberRepository, Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new NotFoundDBEntityException("존재하지 않는 사용자의 id값 입니다.");
        }
    }

    public static void checkBlockedMember(MemberBlockRepository memberBlockRepository, Member blocker, Member blockedMember) {
        MemberBlock memberBlock = memberBlockRepository.findByBlockerAndBlocked(blocker, blockedMember)
                .orElseThrow(() -> new NotFoundDBEntityException("차단되지 않은 사용자입니다."));
        if (memberBlock.getIsBlocked()) {
            throw new ClientBadRequestException("차단한 사용자의 게시글은 조회할 수 없습니다.");
        }
    }
}
