package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.dto.MemberSaveRequest;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public Member getMemberById (Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new NotFoundDBEntityException("Member"));
    }

    public Member createMember (MemberSaveRequest request) {
        return memberRepository.save(Member.builder()
                .name(request.name())
                .authUserId(request.authId())
                .generation(request.generation())
                .build()
        );
    }

    public Member getMemberByName (String name) {
        return memberRepository.findByName(name).orElseThrow(() -> new NotFoundDBEntityException("Member"));
    }

}
