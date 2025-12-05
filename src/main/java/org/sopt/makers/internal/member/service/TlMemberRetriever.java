package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.TlMember;
import org.sopt.makers.internal.member.repository.TlMemberRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TlMemberRetriever {

    private final TlMemberRepository tlMemberRepository;

    public List<TlMember> findByTlGeneration(Integer tlGeneration) {
        return tlMemberRepository.findByTlGeneration(tlGeneration);
    }
}
