package org.sopt.makers.internal.common;

//@RequiredArgsConstructor
//@Service
//public class CustomMemberDetailsService implements UserDetailsService {
//
//    private final MemberRepository memberRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String userIdStr) {
//        val userId = Long.parseLong(userIdStr);
//        val member = memberRepository.findById(userId).orElseThrow(() -> new NotFoundDBEntityException("Member"));
//        return new InternalMemberDetails(member);
//    }
//}
// 인증중앙화 삭제 예정
