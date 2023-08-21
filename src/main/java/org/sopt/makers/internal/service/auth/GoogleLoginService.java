package org.sopt.makers.internal.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.domain.GoogleTokenManager;
import org.sopt.makers.internal.exception.AuthFailureException;
import org.sopt.makers.internal.exception.WrongTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleLoginService implements SocialLoginService {

    private final GoogleTokenManager googleTokenManager;

    @Transactional
    public String getUserSocialInfo(String type, String code) {
        val googleAccessTokenResponse = googleTokenManager.getAccessTokenByCode(code, type);
        if (googleAccessTokenResponse == null) { throw new AuthFailureException("Google 인증에 실패했습니다.");}
        val googleAccessToken = googleAccessTokenResponse.idToken();
        val googleUserInfo = googleTokenManager.getUserInfo(googleAccessToken);
        if (googleUserInfo == null) throw new WrongTokenException("Google AccessToken Invalid");
        log.info("Google user id : " + googleUserInfo);
        return googleUserInfo;
    }
}