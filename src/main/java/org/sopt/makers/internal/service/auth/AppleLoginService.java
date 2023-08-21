package org.sopt.makers.internal.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.domain.AppleTokenManager;
import org.sopt.makers.internal.exception.AuthFailureException;
import org.sopt.makers.internal.exception.WrongTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleLoginService implements SocialLoginService {

    private final AppleTokenManager appleTokenManager;

    @Transactional
    public String getUserSocialInfo(String type, String code) {
        val appleAccessTokenResponse = appleTokenManager.getAccessTokenByCode(code);
        if (appleAccessTokenResponse == null) throw new AuthFailureException("Apple 인증에 실패했습니다.");
        val appleUserInfo = appleTokenManager.getUserInfo(appleAccessTokenResponse);
        if (appleUserInfo == null) throw new WrongTokenException("Apple AccessToken Invalid");
        log.info("Apple user id : " + appleUserInfo);
        return appleUserInfo;
    }
}