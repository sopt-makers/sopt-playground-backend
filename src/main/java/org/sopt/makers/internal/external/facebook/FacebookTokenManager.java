package org.sopt.makers.internal.external.facebook;

//@Service
//@AllArgsConstructor
//public class FacebookTokenManager {
//    private final AuthConfig authConfig;
//    private final FacebookAuthClient authClient;
//
//    public String getAccessTokenByCode(String code, String redirectType) {
//        val redirectUri = Objects.equals(redirectType, "auth") ? authConfig.getFbRedirectUriAuth() : authConfig.getFbRedirectUriRegister();
//        val accessTokenResponse = authClient.getAccessToken(
//                authConfig.getFbClientAppId(), redirectUri, authConfig.getFbClientSecret(), code
//        );
//        return accessTokenResponse.accessToken();
//    }
//
//    public FacebookUserInfoResponse getUserInfo(String accessToken) {
//        return authClient.getUserInfo(accessToken);
//    }
//
//}
