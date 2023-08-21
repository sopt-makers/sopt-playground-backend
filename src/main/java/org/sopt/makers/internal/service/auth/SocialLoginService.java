package org.sopt.makers.internal.service.auth;

public interface SocialLoginService {

    String getUserSocialInfo(String type, String code);
}