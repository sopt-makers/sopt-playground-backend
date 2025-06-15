package org.sopt.makers.internal.common.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.external.apple.AppleTokenManager;
import org.sopt.makers.internal.external.facebook.FacebookTokenManager;
import org.sopt.makers.internal.external.google.GoogleTokenManager;
import org.sopt.makers.internal.exception.AuthFailureException;
import org.sopt.makers.internal.exception.WrongSixNumberCodeException;
import org.sopt.makers.internal.exception.WrongTokenException;
import org.sopt.makers.internal.external.message.email.EmailSender;
import org.sopt.makers.internal.external.message.gabia.GabiaService;
import org.sopt.makers.internal.external.message.sms.SmsSender;
import org.sopt.makers.internal.internal.InternalTokenManager;
import org.sopt.makers.internal.member.domain.SoptMemberHistory;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberSoptActivity;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.member.repository.soptactivity.MemberSoptActivityRepository;
import org.sopt.makers.internal.member.repository.SoptMemberHistoryRepository;
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
    private final MemberSoptActivityRepository memberSoptActivityRepository;
    private final AuthConfig authConfig;
    private final InternalTokenManager tokenManager;
    private final FacebookTokenManager fbTokenManager;
    private final GoogleTokenManager googleTokenManager;

    private final AppleTokenManager appleTokenManager;

    private final MemberRepository memberRepository;
    private final SoptMemberHistoryRepository soptMemberHistoryRepository;
    private final EmailSender emailSender;

    private final GabiaService gabiaService;
    private final SmsSender smsSender; // 삭제 필요

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
    public String registerByGoogleAndMagicRegisterToken(String registerToken, String code, String state) {
        if(!state.equals("register") && !state.equals("change")) throw new AuthFailureException("잘못된 경로입니다.");
        val isMagic = tokenManager.verifyMagicRegisterToken(registerToken);
        val googleAccessTokenResponse = googleTokenManager.getAccessTokenByCode(code, "register");
        if (!isMagic) throw new WrongTokenException("tokenInvalid");
        if (googleAccessTokenResponse == null) throw new AuthFailureException("google 인증에 실패했습니다.");
        val googleAccessToken = googleAccessTokenResponse.idToken();

        val googleUserInfo = googleTokenManager.getUserInfo(googleAccessToken);
        if (googleUserInfo == null) throw new WrongTokenException("Google AccessToken Invalid");
        val member = memberRepository.findByName("User1")
                .orElseThrow(() -> new EntityNotFoundException("Test 유저를 찾을 수 없습니다."));
        member.updateMemberAuth(googleUserInfo.sub(), "google");

        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String registerByDevQaMagicRegisterToken(String registerToken, String code, String socialType) {
        val isMagic = tokenManager.verifyDevMagicRegisterQaToken(registerToken);
        if (!isMagic) throw new WrongTokenException("tokenInvalid");

        String authUserId = null;
        if(Objects.equals(socialType, "facebook")) {
            val fbAccessToken = fbTokenManager.getAccessTokenByCode(code, "register");
            if (fbAccessToken == null) throw new AuthFailureException("facebook 인증에 실패했습니다.");
            val fbUserInfo = fbTokenManager.getUserInfo(fbAccessToken);
            authUserId = fbUserInfo.userId();
        }
        else if(Objects.equals(socialType, "google")) {
            val googleAccessTokenResponse = googleTokenManager.getAccessTokenByCode(code, "register");
            if (googleAccessTokenResponse == null) throw new AuthFailureException("google 인증에 실패했습니다.");
            val googleAccessToken = googleAccessTokenResponse.idToken();

            val googleUserInfo = googleTokenManager.getUserInfo(googleAccessToken);
            if (googleUserInfo == null) throw new WrongTokenException("Google AccessToken Invalid");
            authUserId = googleUserInfo.sub();
        }
        else if(Objects.equals(socialType, "apple")) {
            val appleAccessTokenResponse = appleTokenManager.getAccessTokenByCode(code);
            if (appleAccessTokenResponse == null) throw new AuthFailureException("apple 인증에 실패했습니다.");

            val appleUserInfo = appleTokenManager.getUserInfo(appleAccessTokenResponse);
            if (appleUserInfo == null) throw new WrongTokenException("Apple AccessToken Invalid");
            authUserId = appleUserInfo;
        }
        if(authUserId == null) throw new AuthFailureException("잘못된 소셜로그인입니다.");
        val member = memberRepository.save(
                Member.builder()
                        .authUserId(authUserId)
                        .idpType(socialType)
                        .name("DEV TESTER")
                        .hasProfile(true)
                        .build()
        );
        return tokenManager.createAuthToken(member.getId());
    }

    @Deprecated(since = "2025.06.15 인증중앙화 정리")
    @Transactional
    public String registerByFbAndMagicRegisterToken(String registerToken, String code) {
        val isMagic = tokenManager.verifyMagicRegisterToken(registerToken);
        val fbAccessToken = fbTokenManager.getAccessTokenByCode(code, "register");
        if (!isMagic) throw new WrongTokenException("tokenInvalid");
        if (fbAccessToken == null) throw new AuthFailureException("facebook 인증에 실패했습니다.");

        val fbUserInfo = fbTokenManager.getUserInfo(fbAccessToken);
        val member = memberRepository.findByName("User1")
                .orElseThrow(() -> new EntityNotFoundException("Test 유저를 찾을 수 없습니다."));
        member.updateMemberAuth(fbUserInfo.userId(), "facebook");

        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String registerByAppleAndMagicRegisterToken(String registerToken, String code, String state) {
        if(!state.equals("register") && !state.equals("change")) throw new AuthFailureException("잘못된 경로입니다.");
        val isMagic = tokenManager.verifyMagicRegisterToken(registerToken);
        val appleAccessTokenResponse = appleTokenManager.getAccessTokenByCode(code);
        if (!isMagic) throw new WrongTokenException("tokenInvalid");
        if (appleAccessTokenResponse == null) throw new AuthFailureException("apple 인증에 실패했습니다.");

        val appleUserInfo = appleTokenManager.getUserInfo(appleAccessTokenResponse);
        if (appleUserInfo == null) throw new WrongTokenException("Apple AccessToken Invalid");
        val member = memberRepository.findByName("User1")
                .orElseThrow(() -> new EntityNotFoundException("Test 유저를 찾을 수 없습니다."));
        member.updateMemberAuth(appleUserInfo, "apple");

        return tokenManager.createAuthToken(member.getId());
    }

    @Deprecated(since = "2025.06.15 인증중앙화 정리")
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

    @Deprecated(since = "2025.06.15 인증중앙화 정리")
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
        val member = insertMemberAndActivityData("facebook", fbUserInfo.userId(), memberHistories);

        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String authByGoogle (String code) {
        // 1. 구글 Access Token 요청
        val googleAccessTokenResponse = googleTokenManager.getAccessTokenByCode(code, "auth");

        // 2. 토큰 발급 실패 시 예외 발생
        if (googleAccessTokenResponse == null) {
            throw new AuthFailureException("Google 인증에 실패했습니다.");
        }

        // 3. Access Token에서 idToken 추출
        val googleAccessToken = googleAccessTokenResponse.idToken();

        // 4. idToken으로 사용자 정보 조회
        val googleUserInfoResponse = googleTokenManager.getUserInfo(googleAccessToken);
        log.info("Google user id : " + googleUserInfoResponse);

        // 5. 해당 Google 사용자 ID로 회원 조회, 없으면 예외 발생
        val member = memberRepository.findByAuthUserId(googleUserInfoResponse.sub())
                .orElseThrow(() -> new AuthFailureException("SOPT.org 회원이 아닙니다. [Google] : " + googleUserInfoResponse));

        // 6. 회원 이메일 정보 확인, null이면 해당 이메일로 변경해줌
        checkEmailIsNull(member, googleUserInfoResponse.email());

        // 7. 최종적으로 인증 토큰 생성 후 반환
        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String registerByGoogle(String registerToken, String code, String state) {
        // 1. 상태값이 register 또는 change가 아니면 예외
        if(!state.equals("register") && !state.equals("change")) throw new AuthFailureException("잘못된 경로입니다.");

        // 2. 회원가입 토큰 검증
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);

        // 3. 구글 인증 코드로 액세스 토큰 요청
        val googleAccessTokenResponse = googleTokenManager.getAccessTokenByCode(code, "register");

        // 4. 토큰 정보 또는 구글 토큰 응답이 없으면 예외
        if (registerTokenInfo == null) throw new WrongTokenException("tokenInvalid");
        if (googleAccessTokenResponse == null) throw new AuthFailureException("google 인증에 실패했습니다.");

        // 5. 구글 idToken 추출
        val googleAccessToken = googleAccessTokenResponse.idToken();

        // 6. 회원 이력 조회
        val memberHistories = findAllMemberHistoriesByRegisterTokenInfo(registerTokenInfo);
        if (memberHistories.isEmpty()) throw new EntityNotFoundException("Sopt Member History's email or phone" + registerTokenInfo + " not found");

        // 7. register 상태인데 이미 가입된 이력이 있으면 예외
        if (state.equals("register") && memberHistories.stream().anyMatch(SoptMemberHistory::getIsJoined))
            throw new AuthFailureException("이미 가입된 사용자입니다.");

        // 8. 첫 번째 회원 이력 가져오기
        val memberHistory = memberHistories.get(0);

        // 9. 구글 사용자 정보 조회
        val googleUserInfo = googleTokenManager.getUserInfo(googleAccessToken);
        if (googleUserInfo == null) throw new WrongTokenException("Google AccessToken Invalid");

        // 10. 상태에 따라 회원 정보 변경 또는 신규 등록
        val member = state.equals("change")
                ? changeMemberSocialData(memberHistory.getPhone(), "google", googleUserInfo.sub())
                : insertMemberAndActivityData("google", googleUserInfo.sub(), memberHistories);

        // 11. 인증 토큰 생성 및 반환
        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String authByApple (String code) {
        // 1. 애플 인증 코드로 액세스 토큰 요청
        val appleAccessTokenResponse = appleTokenManager.getAccessTokenByCode(code);

        // 2. 토큰 발급 실패 시 예외 처리
        if (appleAccessTokenResponse == null) {
            throw new AuthFailureException("Apple 인증에 실패했습니다.");
        }

        // 3. 액세스 토큰으로 사용자 정보 조회
        val appleUserInfo = appleTokenManager.getUserInfo(appleAccessTokenResponse);
        log.info("Apple user id : " + appleUserInfo);

        // 4. 사용자 정보로 회원 조회, 없으면 예외 발생
        val member = memberRepository.findByAuthUserId(appleUserInfo)
                .orElseThrow(() -> new AuthFailureException("SOPT.org 회원이 아닙니다. [Apple] : " +  appleUserInfo));

        // 5. 인증 토큰 생성 및 반환
        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    public String registerByApple (String registerToken, String code, String state) {
        // 1. 상태값이 register 또는 change가 아니면 예외
        if(!state.equals("register") && !state.equals("change")) throw new AuthFailureException("잘못된 경로입니다.");

        // 2. 회원가입 토큰 검증
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);

        // 3. 애플 인증 코드로 액세스 토큰 요청
        val appleAccessTokenResponse = appleTokenManager.getAccessTokenByCode(code);

        // 4. 토큰 정보 또는 애플 토큰 응답이 없으면 예외
        if (registerTokenInfo == null) throw new WrongTokenException("tokenInvalid");
        if (appleAccessTokenResponse == null) throw new AuthFailureException("apple 인증에 실패했습니다.");

        // 5. 회원 이력 조회
        val memberHistories = findAllMemberHistoriesByRegisterTokenInfo(registerTokenInfo);

        // 6. register 상태인데 이미 가입된 이력이 있으면 예외
        if (memberHistories.isEmpty()) throw new EntityNotFoundException("Sopt Member History's email or phone" + registerTokenInfo + " not found");
        if (state.equals("register") && memberHistories.stream().anyMatch(SoptMemberHistory::getIsJoined)) throw new AuthFailureException("이미 가입된 사용자입니다.");

        // 7. 첫 번째 회원 이력 가져오기
        val memberHistory = memberHistories.get(0);

        // 8. 애플 사용자 정보 조회
        val appleUserInfo = appleTokenManager.getUserInfo(appleAccessTokenResponse);
        if (appleUserInfo == null) throw new WrongTokenException("Apple AccessToken Invalid");

        // 9. 상태에 따라 회원 정보 변경 또는 신규 등록
        val member = state.equals("change")
                ? changeMemberSocialData(memberHistory.getPhone(), "apple", appleUserInfo)
                : insertMemberAndActivityData("apple", appleUserInfo, memberHistories);

        // 10. 인증 토큰 생성 및 반환
        return tokenManager.createAuthToken(member.getId());
    }

    @Transactional
    // 회원가입 토큰이 전화번호인지 이메일인지 판별해 해당 정보로 최신 회원 이력을 조회하는 메소드
    public Optional<SoptMemberHistory> findMemberByRegisterToken (String registerToken) {
        val registerTokenInfo = tokenManager.verifyRegisterToken(registerToken);
        if(registerTokenInfo.startsWith("010")){
            return soptMemberHistoryRepository.findTopByPhoneOrderByIdDesc(registerTokenInfo);
        } else {
            return soptMemberHistoryRepository.findTopByEmailOrderByIdDesc(registerTokenInfo);
        }
    }

    @Transactional
    // 회원가입 토큰 정보가 전화번호인지 이메일인지에 따라, 해당 정보로 회원의 모든 이력을 최신순으로 조회하는 메소드
    public List<SoptMemberHistory> findAllMemberHistoriesByRegisterTokenInfo (String registerTokenInfo) {
        if(registerTokenInfo.startsWith("010")){
            return soptMemberHistoryRepository.findAllByPhoneOrderByIdDesc(registerTokenInfo);
        } else {
            return soptMemberHistoryRepository.findAllByEmailOrderByIdDesc(registerTokenInfo);
        }
    }

    @Transactional
    // 이메일로 회원가입 링크를 전송하고, 전송 결과를 문자열로 반환하는 메소드
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

    // 전화번호로 회원을 찾아 소셜 로그인 정보를 갱신하는 메소드
    private Member changeMemberSocialData(String phone, String idpType, String userInfoId) {
        val member = memberRepository.findByPhone(phone)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));
        member.updateMemberAuth(userInfoId, idpType);
        return member;
    }

    // 회원 이력을 기반으로 새 회원과 활동 이력을 등록하는 메소드
    private Member insertMemberAndActivityData (String idpType, String userInfoId, List<SoptMemberHistory> memberHistories) {
        val memberHistory = memberHistories.get(0);
        val member = memberRepository.save(
                Member.builder()
                        .authUserId(userInfoId)
                        .idpType(idpType)
                        .name(memberHistory.getName())
                        .email(memberHistory.getEmail())
                        .phone(memberHistory.getPhone())
                        .generation(memberHistory.getGeneration())
                        .hasProfile(true)
                        .build()
        );
        val memberActivities = memberHistories.stream().map(soptMemberHistory -> {
            // organizer 테이블 안쓴다고 들었는데 내 생각에는 프로필 조회 시 운영진 정보도 조회해서 쓰이는 것 같다
            val team = isInSoptOrganizerTeam(soptMemberHistory.getPart()) ? soptMemberHistory.getPart() : null;
            return MemberSoptActivity.builder()
                    .memberId(member.getId())
                    .team(team)
                    .part(soptMemberHistory.getPart())
                    .generation(soptMemberHistory.getGeneration())
                    .build();
        }).toList();
        memberSoptActivityRepository.saveAll(memberActivities);
        memberHistory.makeMemberJoin();
        return member;
    }

    // 핸드폰으로 6문자 인증코드 보내기
    public String sendSixNumberSmsCode (String phone, String state) {
        val memberHistories = soptMemberHistoryRepository.findAllByPhoneOrderByIdDesc(phone);
        if (memberHistories.isEmpty()) return "emptySoptUser";
        if (state.equals("registration") && memberHistories.stream().anyMatch(SoptMemberHistory::getIsJoined)) return "alreadyTaken";

        val exp = LocalDateTime.now(KST).plusMinutes(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        val smsToken = phone + "@" + exp;
        var sixNumberCode = getRandomNumberString();
        var isExistedCode = memberAndSmsTokenMap.putIfAbsent(sixNumberCode, smsToken) != null;
        if (isExistedCode) return "shouldRetry";

        val message = "[SOPT Makers] 인증번호 [" + sixNumberCode + "]를 입력해주세요.";
        log.info(message);
        gabiaService.sendSMS(phone, message);
        clearMapByRandomAccess();
        return "success";
    }

    // 6문자 인증 코드로 토큰 받기
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

    public String getRegisterQaToken() {
        return authConfig.getDevRegisterQaToken();
    }

    private boolean isInSoptOrganizerTeam(String part) {
        if (part == null) return false;
        return (part.contains("장") || part.equals("총무"));
    }

    // 만료된 SMS 토큰이 있을 경우, 해당 토큰을 가진 맵의 엔트리들을 삭제하는 메소드
    private void clearMapByRandomAccess () {
        log.info("[Before clear Map] Map size : {}", memberAndSmsTokenMap.size());
        val isMapEmpty = memberAndSmsTokenMap.isEmpty();
        if (!isMapEmpty) {
            val smsToken = memberAndSmsTokenMap.entrySet().iterator().next().getValue();
            val isExpiredSmsToken = checkIsExpiredSmsToken(smsToken);
            if (isExpiredSmsToken) {
                memberAndSmsTokenMap.keySet().removeAll(findShouldDeleteKeys());
                log.info("[After clear Map] Map size : {}", memberAndSmsTokenMap.size());
            }
        }
    }

    // 만료된 SMS 토큰을 가진 맵의 키들을 찾아 반환하는 메소드
    private Set<String> findShouldDeleteKeys () {
        val shouldDeleteKeys = new HashSet<String>();
        for (val elem: memberAndSmsTokenMap.entrySet()) {
            val isTokenShouldBeDeleted = checkIsExpiredSmsToken(elem.getValue());
            if (isTokenShouldBeDeleted) shouldDeleteKeys.add(elem.getKey());
        }
        return shouldDeleteKeys;
    }

    // 주어진 SMS 토큰이 만료되었는지 확인하는 메소드
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

    private void checkEmailIsNull(Member member, String email) {
        if (member.getEmail() == null) {
            member.changeEmail(email);
        }
    }
}
