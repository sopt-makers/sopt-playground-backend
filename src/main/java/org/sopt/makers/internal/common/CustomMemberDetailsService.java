package org.sopt.makers.internal.common;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.internal.InternalMemberDetails;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomMemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdStr) {
        val userId = Long.parseLong(userIdStr);
        val member = memberRepository.findById(userId).orElseThrow(() -> new NotFoundDBEntityException("Member"));
        return new InternalMemberDetails(member);
    }
}
