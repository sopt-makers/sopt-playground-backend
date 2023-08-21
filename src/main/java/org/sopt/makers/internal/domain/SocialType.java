package org.sopt.makers.internal.domain;

import org.sopt.makers.internal.exception.AuthFailureException;

import java.util.Arrays;

public enum SocialType {
    facebook, google, apple;

    public static SocialType from(String social) {
        return Arrays.stream(SocialType.values())
                .filter(socialType -> socialType.toString().equals(social))
                .findFirst()
                .orElseThrow(() -> new AuthFailureException("없는 소셜로그인 요청입니다."));
    }
}
