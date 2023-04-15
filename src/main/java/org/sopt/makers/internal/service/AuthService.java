package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.domain.*;
import org.sopt.makers.internal.dto.auth.NaverSmsRequest;
import org.sopt.makers.internal.exception.AuthFailureException;
import org.sopt.makers.internal.exception.WrongSixNumberCodeException;
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
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final AuthConfig authConfig;
    private final InternalTokenManager tokenManager;
    private final FacebookTokenManager fbTokenManager;
    private final GoogleTokenManager googleTokenManager;

    private final AppleTokenManager appleTokenManager;

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
    public String registerByGoogleAndMagicRegisterToken(String registerToken, String code) {
        val isMagic = tokenManager.verifyMagicRegisterToken(registerToken);
        val googleAccessTokenResponse = googleTokenManager.getAccessTokenByCode(code, "register");
        if (!isMagic) throw new WrongTokenException("tokenInvalid");
        if (googleAccessTokenResponse == null) throw new AuthFailureException("google 인증에 실패했습니다.");
        val googleAccessToken = googleAccessTokenResponse.idToken();

        val googleUserInfo = googleTokenManager.getUserInfo(googleAccessToken);
        if (googleUserInfo == null) throw new WrongTokenException("Google AccessToken Invalid");
        val member = memberRepository.findByName("Tester")
                .orElseThrow(() -> new EntityNotFoundException("Test 유저를 찾을 수 없습니다."));
        member.updateMemberAuth(googleUserInfo, "google");

        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String registerByFbAndMagicRegisterToken(String registerToken, String code) {
        val isMagic = tokenManager.verifyMagicRegisterToken(registerToken);
        val fbAccessToken = fbTokenManager.getAccessTokenByCode(code, "register");
        if (!isMagic) throw new WrongTokenException("tokenInvalid");
        if (fbAccessToken == null) throw new AuthFailureException("facebook 인증에 실패했습니다.");

        val fbUserInfo = fbTokenManager.getUserInfo(fbAccessToken);
        val member = memberRepository.findByName("Tester")
                .orElseThrow(() -> new EntityNotFoundException("Test 유저를 찾을 수 없습니다."));
        member.updateMemberAuth(fbUserInfo.userId(), "facebook");

        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String registerByAppleAndMagicRegisterToken(String registerToken, String code) {
        val isMagic = tokenManager.verifyMagicRegisterToken(registerToken);
        val appleAccessTokenResponse = appleTokenManager.getAccessTokenByCode(code);
        if (!isMagic) throw new WrongTokenException("tokenInvalid");
        if (appleAccessTokenResponse == null) throw new AuthFailureException("apple 인증에 실패했습니다.");

        val appleUserInfo = appleTokenManager.getUserInfo(appleAccessTokenResponse);
        if (appleUserInfo == null) throw new WrongTokenException("Apple AccessToken Invalid");
        val member = memberRepository.findByName("Tester")
                .orElseThrow(() -> new EntityNotFoundException("Test 유저를 찾을 수 없습니다."));
        member.updateMemberAuth(appleUserInfo, "apple");

        return tokenManager.createAuthToken(member.getId());
    }


    @Transactional
    public String authByFb (String code) {
        val fbAccessToken = fbTokenManager.getAccessTokenByCode(code, "auth");
        if (fbAccessToken == null) {
            throw new AuthFailureException("Facebook 인증에 실패했습니다.");
        }
        val fbUserInfo = fbTokenManager.getUserInfo(fbAccessToken);
        log.info("Facebook user id : " + fbUserInfo.userId() + " / name : " + fbUserInfo.userName());
        val member = memberRepository.findByAuthUserId(fbUserInfo.userId())
                .orElseThrow(() -> new AuthFailureException("SOPT.org 회원이 아닙니다.[Facebook] : " + fbUserInfo.userId()));

        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String registerByFb (String registerToken, String code) {
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);
        val fbAccessToken = fbTokenManager.getAccessTokenByCode(code, "register");
        if (registerTokenInfo == null) throw new WrongTokenException("tokenInvalid");
        if (fbAccessToken == null) throw new AuthFailureException("facebook 인증에 실패했습니다.");

        val memberHistories = findAllMemberHistoriesByRegisterTokenInfo(registerTokenInfo);
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
                        .phone(memberHistory.getPhone())
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
        log.info("Google user id : " + googleUserInfoResponse);
        val member = memberRepository.findByAuthUserId(googleUserInfoResponse)
                .orElseThrow(() -> new AuthFailureException("SOPT.org 회원이 아닙니다. [Google] : " + googleUserInfoResponse));

        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String registerByGoogle(String registerToken, String code) {
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);
        val googleAccessTokenResponse = googleTokenManager.getAccessTokenByCode(code, "register");
        if (registerTokenInfo == null) throw new WrongTokenException("tokenInvalid");
        if (googleAccessTokenResponse == null) throw new AuthFailureException("google 인증에 실패했습니다.");
        val googleAccessToken = googleAccessTokenResponse.idToken();

        val memberHistories = findAllMemberHistoriesByRegisterTokenInfo(registerTokenInfo);
        if (memberHistories.isEmpty()) throw new EntityNotFoundException("Sopt Member History's email or phone" + registerTokenInfo + " not found");
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
                        .phone(memberHistory.getPhone())
                        .generation(memberHistory.getGeneration())
                        .build()
        );
        memberHistory.makeMemberJoin();

        return tokenManager.createAuthToken(member.getId());

    }

    @Transactional
    public String authByApple (String code) {
        val appleAccessTokenResponse = appleTokenManager.getAccessTokenByCode(code);
        if (appleAccessTokenResponse == null) {
            throw new AuthFailureException("Apple 인증에 실패했습니다.");
        }
        val appleUserInfo = appleTokenManager.getUserInfo(appleAccessTokenResponse);
        log.info("Apple user id : " + appleUserInfo);
        val member = memberRepository.findByAuthUserId(appleUserInfo)
                .orElseThrow(() -> new AuthFailureException("SOPT.org 회원이 아닙니다. [Apple] : " +  appleUserInfo));

        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String registerByApple (String registerToken, String code) {
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);
        val appleAccessTokenResponse = appleTokenManager.getAccessTokenByCode(code);
        if (registerTokenInfo == null) throw new WrongTokenException("tokenInvalid");
        if (appleAccessTokenResponse == null) throw new AuthFailureException("apple 인증에 실패했습니다.");

        val memberHistories = findAllMemberHistoriesByRegisterTokenInfo(registerTokenInfo);
        if (memberHistories.isEmpty()) throw new EntityNotFoundException("Sopt Member History's email or phone" + registerTokenInfo + " not found");
        if (memberHistories.stream().anyMatch(SoptMemberHistory::getIsJoined)) throw new AuthFailureException("이미 가입된 사용자입니다.");

        val memberHistory = memberHistories.get(0);
        val appleUserInfo = appleTokenManager.getUserInfo(appleAccessTokenResponse);
        if (appleUserInfo == null) throw new WrongTokenException("Apple AccessToken Invalid");
        val member = memberRepository.save(
                Member.builder()
                        .authUserId(appleUserInfo)
                        .idpType("apple")
                        .name(memberHistory.getName())
                        .email(memberHistory.getEmail())
                        .phone(memberHistory.getPhone())
                        .generation(memberHistory.getGeneration())
                        .build()
        );
        memberHistory.makeMemberJoin();

        return tokenManager.createAuthToken(member.getId());

    }

    @Transactional
    public Optional<SoptMemberHistory> findMemberByRegisterToken (String registerToken) {
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);
        if(registerTokenInfo.startsWith("010")){
            return soptMemberHistoryRepository.findTopByPhoneOrderByIdDesc(registerTokenInfo);
        } else {
            return soptMemberHistoryRepository.findTopByEmailOrderByIdDesc(registerTokenInfo);
        }
    }

    @Transactional
    public List<SoptMemberHistory> findAllMemberHistoriesByRegisterTokenInfo (String registerTokenInfo) {
        if(registerTokenInfo.startsWith("010")){
            return soptMemberHistoryRepository.findAllByPhoneOrderByIdDesc(registerTokenInfo);
        } else {
            return soptMemberHistoryRepository.findAllByEmailOrderByIdDesc(registerTokenInfo);
        }
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

    public String sendSixNumberSmsCode (String phone) {
        val memberHistories = soptMemberHistoryRepository.findAllByPhoneOrderByIdDesc(phone);
        if (memberHistories.isEmpty()) return "emptySoptUser";
        if (memberHistories.stream().anyMatch(SoptMemberHistory::getIsJoined)) return "alreadyTaken";

        val exp = LocalDateTime.now(KST).plusMinutes(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        val smsToken = phone + "@" + exp;
        var sixNumberCode = getRandomNumberString();
        var isExistedCode = memberAndSmsTokenMap.putIfAbsent(sixNumberCode, smsToken) != null;
        if (isExistedCode) return "shouldRetry";

        val message = "[SOPT Makers] 인증번호 [" + sixNumberCode + "]를 입력해주세요.";
        log.info(message);
        smsSender.sendSms(new NaverSmsRequest.SmsMessage(phone, message));
        clearMapByRandomAccess();
        return "success";
    }

    public String getRegisterTokenBySixNumberCode (String sixNumberCode) {
        val smsToken = memberAndSmsTokenMap.get(sixNumberCode);
        if (smsToken == null) throw new WrongSixNumberCodeException("notExistedCode");

        memberAndSmsTokenMap.remove(sixNumberCode);
        val isExpiredSmsToken = checkIsExpiredSmsToken(smsToken);
        if (isExpiredSmsToken) throw new WrongSixNumberCodeException("expiredCode");
        val phone = smsToken.split("@")[0];
        return tokenManager.createRegisterToken(phone);
    }

    public String getRegisterTokenByMagicNumber () {
        return authConfig.getMagicRegisterToken();
    }


    private void clearMapByRandomAccess () {
        log.info("[Before clear Map] Map size : " + memberAndSmsTokenMap.size());
        val isMapEmpty = memberAndSmsTokenMap.size() == 0;
        if (!isMapEmpty) {
            val smsToken = memberAndSmsTokenMap.entrySet().iterator().next().getValue();
            val isExpiredSmsToken = checkIsExpiredSmsToken(smsToken);
            if (isExpiredSmsToken) {
                memberAndSmsTokenMap.keySet().removeAll(findShouldDeleteKeys());
                log.info("[After clear Map] Map size : " + memberAndSmsTokenMap.size());
            }
        }
    }

    private Set<String> findShouldDeleteKeys () {
        val shouldDeleteKeys = new HashSet<String>();
        for (val elem: memberAndSmsTokenMap.entrySet()) {
            val isTokenShouldBeDeleted = checkIsExpiredSmsToken(elem.getValue());
            if (isTokenShouldBeDeleted) shouldDeleteKeys.add(elem.getKey());
        }
        return shouldDeleteKeys;
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
