package org.sopt.makers.internal.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.domain.FacebookTokenManager;
import org.sopt.makers.internal.exception.AuthFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookLoginService implements SocialLoginService{

    private final FacebookTokenManager fbTokenManager;

    @Transactional
    public String getUserSocialInfo(String type, String code) {
        val fbAccessToken = fbTokenManager.getAccessTokenByCode(code, type);
        if (fbAccessToken == null) throw new AuthFailureException("Facebook 인증에 실패했습니다.");
        val fbUserInfo = fbTokenManager.getUserInfo(fbAccessToken);
        log.info("Facebook user id : " + fbUserInfo.userId() + " / name : " + fbUserInfo.userName());
        return fbUserInfo.userId();
    }
}