package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.*;
import org.sopt.makers.internal.dto.auth.NaverSmsRequest;
import org.sopt.makers.internal.exception.AuthFailureException;
import org.sopt.makers.internal.exception.WrongTokenException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.SoptMemberHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final InternalTokenManager tokenManager;
    private final FacebookTokenManager fbTokenManager;
    private final GoogleTokenManager googleTokenManager;

    private final MemberRepository memberRepository;
    private final SoptMemberHistoryRepository soptMemberHistoryRepository;
    private final EmailSender emailSender;

    private final SmsSender smsSender;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final Map<String, String> memberAndSmsTokenMap = new HashMap<>();

    public String createCode(String accessToken) {
        val userId = Long.parseLong(tokenManager.getUserIdFromAuthToken(accessToken));
        return tokenManager.createCode(userId);
    }

    public String authByCode(String code) {
        val userId = tokenManager.getUserIdFromCode(code);
        return tokenManager.createAuthToken(userId);
    }

    @Transactional
    public String authByFb (String code) {
        val fbAccessToken = fbTokenManager.getAccessTokenByCode(code, "auth");
        if (fbAccessToken == null) {
            throw new AuthFailureException("Facebook 인증에 실패했습니다.");
        }
        val fbUserInfo = fbTokenManager.getUserInfo(fbAccessToken);
        val member = memberRepository.findByAuthUserId(fbUserInfo.userId())
                .orElseThrow(() -> new AuthFailureException("SOPT.org 회원이 아닙니다."));

        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String registerByFb (String registerToken, String code) {
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);
        val fbAccessToken = fbTokenManager.getAccessTokenByCode(code, "register");
        if (registerTokenInfo == null) throw new WrongTokenException("tokenInvalid");
        if (fbAccessToken == null) throw new AuthFailureException("facebook 인증에 실패했습니다.");

        val memberHistories = soptMemberHistoryRepository.findAllByEmailOrderByIdDesc(registerTokenInfo);
        if (memberHistories.isEmpty()) throw new EntityNotFoundException("Sopt Member History's email" + registerTokenInfo + " not found");
        if (memberHistories.stream().anyMatch(SoptMemberHistory::getIsJoined)) throw new AuthFailureException("이미 가입된 사용자입니다.");

        val memberHistory = memberHistories.get(0);
        val fbUserInfo = fbTokenManager.getUserInfo(fbAccessToken);
        val member = memberRepository.save(
                Member.builder()
                        .authUserId(fbUserInfo.userId())
                        .idpType("facebook")
                        .name(memberHistory.getName())
                        .email(memberHistory.getEmail())
                        .generation(memberHistory.getGeneration())
                        .build()
        );
        memberHistory.makeMemberJoin();

        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String authByGoogle (String code) {
        val googleAccessTokenResponse = googleTokenManager.getAccessTokenByCode(code, "auth");
        if (googleAccessTokenResponse == null) {
            throw new AuthFailureException("Google 인증에 실패했습니다.");
        }
        val googleAccessToken = googleAccessTokenResponse.idToken();
        val googleUserInfoResponse = googleTokenManager.getUserInfo(googleAccessToken);
        val member = memberRepository.findByAuthUserId(googleUserInfoResponse)
                .orElseThrow(() -> new AuthFailureException("SOPT.org 회원이 아닙니다."));

        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String registerByGoogle(String registerToken, String code) {
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);
        val googleAccessTokenResponse = googleTokenManager.getAccessTokenByCode(code, "register");
        if (registerTokenInfo == null) throw new WrongTokenException("tokenInvalid");
        if (googleAccessTokenResponse == null) throw new AuthFailureException("google 인증에 실패했습니다.");
        val googleAccessToken = googleAccessTokenResponse.idToken();

        val memberHistories = soptMemberHistoryRepository.findAllByEmailOrderByIdDesc(registerTokenInfo);
        if (memberHistories.isEmpty()) throw new EntityNotFoundException("Sopt Member History's email" + registerTokenInfo + " not found");
        if (memberHistories.stream().anyMatch(SoptMemberHistory::getIsJoined)) throw new AuthFailureException("이미 가입된 사용자입니다.");

        val memberHistory = memberHistories.get(0);
        val googleUserInfo = googleTokenManager.getUserInfo(googleAccessToken);
        if (googleUserInfo == null) throw new WrongTokenException("Google AccessToken Invalid");
        val member = memberRepository.save(
                Member.builder()
                        .authUserId(googleUserInfo)
                        .idpType("google")
                        .name(memberHistory.getName())
                        .email(memberHistory.getEmail())
                        .generation(memberHistory.getGeneration())
                        .build()
        );
        memberHistory.makeMemberJoin();

        return tokenManager.createAuthToken(member.getId());

    }

    @Transactional
    public Optional<SoptMemberHistory> findMemberByRegisterToken (String registerToken) {
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);
        return soptMemberHistoryRepository.findTopByEmailOrderByIdDesc(registerTokenInfo);
    }

    @Transactional
    public String sendRegisterLinkByEmail(String email) {
        val memberHistories = soptMemberHistoryRepository.findAllByEmailOrderByIdDesc(email);
        if (memberHistories.isEmpty()) return "invalidEmail";
        if (memberHistories.stream().anyMatch(SoptMemberHistory::getIsJoined)) return "alreadyTaken";

        val token = tokenManager.createRegisterToken(email);
        val html = emailSender.createRegisterEmailHtml(token);
        try {
            emailSender.sendEmail(email, "SOPT 회원 인증", html);
            return "success";
        } catch (MessagingException | UnsupportedEncodingException exception) {
            exception.printStackTrace();
            return "cannotSendEmail";
        }
    }

    public void sendSixNumberSmsCode (String phone) {
        val memberHistories = soptMemberHistoryRepository.findAllByPhoneOrderByIdDesc(phone);
        if (memberHistories.isEmpty()) throw new AuthFailureException("없는 SOPT User입니다.");
        if (memberHistories.stream().anyMatch(SoptMemberHistory::getIsJoined)) throw new AuthFailureException("이미 가입한 유저입니다.");
        val exp = LocalDateTime.now(KST).plusMinutes(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        val smsToken = phone + "@" + exp;
        var sixNumberCode = getRandomNumberString();
        var isExistedCode = memberAndSmsTokenMap.putIfAbsent(sixNumberCode, smsToken) != null;
        if (isExistedCode) throw new AuthFailureException("다시 시도해주세요.");
        val message = "[SOPT Makers] 인증번호 [" + sixNumberCode + "]를 입력해주세요.";
        System.out.println(message);
        smsSender.sendSms(new NaverSmsRequest.SmsMessage(phone, message));
    }

    public String getRegisterTokenBySixNumberCode (String sixNumberCode) {
        val smsToken = memberAndSmsTokenMap.get(sixNumberCode);
        if (smsToken == null) throw new AuthFailureException("잘못된 숫자 코드입니다. 재시도 해주세요.");
        val isExpiredSmsToken = checkIsExpiredSmsToken(smsToken);
        if (isExpiredSmsToken) throw new AuthFailureException("만료된 숫자 코드입니다. 재시도 해주세요.");
        memberAndSmsTokenMap.remove(sixNumberCode);
        val phone = smsToken.split("@")[0];
        return tokenManager.createRegisterToken(phone);
    }

    public boolean checkIsExpiredSmsToken (String smsToken) {
        val exp = LocalDateTime.parse(smsToken.split("@")[1], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        val now = LocalDateTime.now(KST);
        return exp.isBefore(now);
    }

    private static String getRandomNumberString() {
        val random = new Random();
        int number = random.nextInt(999999);
        return String.format("%06d", number);
    }
}
