package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.*;
import org.sopt.makers.internal.exception.FacebookAuthFailureException;
import org.sopt.makers.internal.exception.WrongTokenException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.SoptMemberHistoryRepository;
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
    private final SoptMemberHistoryRepository soptMemberHistoryRepository;
    private final EmailSender emailSender;
    
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

        val memberHistory = soptMemberHistoryRepository.findByEmail(registerTokenInfo)
                .orElseThrow(() -> new EntityNotFoundException("Sopt Member History's email" + registerTokenInfo + " not found"));
        if (memberHistory.getIsJoined()) throw new FacebookAuthFailureException("이미 가입된 사용자입니다.");

        val fbUserInfo = fbTokenManager.getUserInfo(fbAccessToken);
        val member = memberRepository.save(
                Member.builder()
                        .authUserId(fbUserInfo.userId())
                        .name(memberHistory.getName())
                        .email(memberHistory.getEmail())
                        .generation(memberHistory.getGeneration())
                        .build()
        );
        memberHistory.makeMemberJoin();

        return tokenManager.createAuthToken(member.getId());
    }

    public Optional<SoptMemberHistory> findMemberByRegisterToken (String registerToken) {
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);
        return soptMemberHistoryRepository.findByEmail(registerTokenInfo);
    }

    public String sendRegisterLinkByEmail(String email) {
        val optionalMemberHistory = soptMemberHistoryRepository.findByEmail(email);
        if (optionalMemberHistory.isEmpty()) return "invalidEmail";

        val member = optionalMemberHistory.get();
        if (member.getIsJoined()) return "alreadyTaken";

        val token = tokenManager.createRegisterToken(email);
        val html = emailSender.createRegisterEmailHtml(token);
        try {
            emailSender.sendEmail(email, "SOPT 회원 인증", html);
            return "success";
        } catch (MessagingException exception) {
            exception.printStackTrace();
            return "cannotSendEmail";
        }
    }
}
