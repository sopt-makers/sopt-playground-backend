package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.EmailSender;
import org.sopt.makers.internal.domain.FacebookTokenManager;
import org.sopt.makers.internal.domain.InternalTokenManager;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.exception.FacebookAuthFailureException;
import org.sopt.makers.internal.exception.WrongTokenException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final InternalTokenManager tokenManager;
    private final FacebookTokenManager fbTokenManager;
    private final MemberRepository memberRepository;
    private final EmailSender emailSender;

    @Value("${oauth.registerPage}")
    private final String registerPageUri;

    public String authByFb (String code) {
        val fbAccessToken = fbTokenManager.getAccessTokenByCode(code, "auth");
        if (fbAccessToken == null) {
            throw new FacebookAuthFailureException("Facebook 인증에 실패했습니다.");
        }
        val fbUserInfo = fbTokenManager.getUserInfo(fbAccessToken);
        val member = memberRepository.findByAuthUserId(fbUserInfo.userId())
                .orElseThrow(() -> new FacebookAuthFailureException("SOPT.org 회원이 아닙니다."));

        return tokenManager.createAuthToken(member.getId());
    }

    public String registerByFb (String registerToken, String code) {
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);
        val fbAccessToken = fbTokenManager.getAccessTokenByCode(code, "register");
        if (registerTokenInfo == null) throw new WrongTokenException("tokenInvalid");
        if (fbAccessToken == null) throw new FacebookAuthFailureException("facebook 인증에 실패했습니다.");

        val member = memberRepository.findByEmail(registerTokenInfo)
                .orElseThrow(() -> new EntityNotFoundException("Member email" + registerTokenInfo + " not found"));
        if (member.getIsJoined()) throw new FacebookAuthFailureException("이미 가입된 사용자입니다.");
        member.makeMemberJoin();

        return tokenManager.createAuthToken(member.getId());
    }

    public Optional<Member> findMemberByRegisterToken (String registerToken) {
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);
        return memberRepository.findByEmail(registerTokenInfo);
    }

    public String sendRegisterLinkByEmail(String email) {
        val optionalMember = memberRepository.findByEmail(email);
        if (optionalMember.isEmpty()) return "invalidEmail";

        val member = optionalMember.get();
        if (member.getIsJoined()) return "alreadyTaken";

        val token = tokenManager.createRegisterToken(email);
        val html = emailSender.createRegisterEmailHtml(token, registerPageUri);
        try {
            emailSender.sendEmail(email, "SOPT 회원 인증", html);
            return "success";
        } catch (MessagingException exception) {
            exception.printStackTrace();
            return "cannotSendEmail";
        }
    }
}
