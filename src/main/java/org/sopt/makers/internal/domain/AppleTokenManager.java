package org.sopt.makers.internal.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.sopt.makers.internal.dto.auth.AppleAccessTokenResponse;
import org.sopt.makers.internal.dto.auth.AppleKeysVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.security.PrivateKey;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class AppleTokenManager {

    @Value("${apple.key.url}")
    private String APPLE_PUBLIC_KEYS_URL;

    @Value("${apple.aud}")
    private String AUD;

    @Value("${apple.sub}")
    private String SUB;

    @Value("${apple.team.id}")
    private String TEAM_ID;

    @Value("${apple.key.id}")
    private String KEY_ID;

    @Value("${apple.key.path}")
    private String KEY_PATH;

    @Value("${apple.auth.token.url}")
    private String AUTH_TOKEN_URL;

    @Value("${apple.revoke.url}")
    private String REVOKE_URL;

    private final RestTemplate appleVerifyClient = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AppleAccessTokenResponse getAccessTokenByCode (String code) {
        return generateAppleAuthToken(code)
                .orElseThrow(() -> new IllegalArgumentException("코드가 유효하지 않습니다."));
    }

    public String getUserInfo (AppleAccessTokenResponse tokenResponse) {
        val idToken = tokenResponse.idToken();
        try {
            val signedJWT = SignedJWT.parse(idToken);
            val payload = signedJWT.getJWTClaimsSet();
            val userId = payload.getSubject();
            return userId;
        } catch (ParseException e) {
            throw new IllegalArgumentException("잘못된 토큰이 전달되었습니다.");
        }
    }

    private boolean verifyPublicKey(SignedJWT signedJWT) {
        try {
            val appleKeys = appleVerifyClient.getForEntity(APPLE_PUBLIC_KEYS_URL, AppleKeysVo.class).getBody();
            if (appleKeys == null) return false;

            for (val appleKey : appleKeys.keys()) {
                val rsaKey = (RSAKey) JWK.parse(objectMapper.writeValueAsString(appleKey));
                val rsaPublicKey = rsaKey.toRSAPublicKey();
                val verifier = new RSASSAVerifier(rsaPublicKey);

                if (signedJWT.verify(verifier)) return true;
            }
            return true;
        } catch (ParseException | JsonProcessingException | JOSEException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String createClientSecret() {
        val now = new Date();
        val privateKey = getPrivateKey()
                .orElseThrow(() -> new IllegalArgumentException("Private key 읽기 실패"));

        return Jwts.builder()
                .setHeaderParam("kid", KEY_ID)
                .setHeaderParam("alg", "ES256")
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000))
                .setIssuer(TEAM_ID)
                .setAudience(AUD)
                .setSubject(SUB)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    private Optional<PrivateKey> getPrivateKey() {
        try {
            val resource = new ClassPathResource(KEY_PATH);
            val privateKey = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            val pemReader = new StringReader(privateKey);
            val pemParser = new PEMParser(pemReader);
            val converter = new JcaPEMKeyConverter();
            val object = (PrivateKeyInfo) pemParser.readObject();
            return Optional.of(converter.getPrivateKey(object));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<AppleAccessTokenResponse> generateAppleAuthToken(String code) {
        val tokenRequest = new LinkedMultiValueMap<>();
        val clientSecret = createClientSecret();
        tokenRequest.add("client_id", SUB);
        tokenRequest.add("client_secret", clientSecret);
        tokenRequest.add("code", code);
        tokenRequest.add("grant_type", "authorization_code");

        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        val entity = new HttpEntity<>(tokenRequest, headers);

        try {
            val responseEntity = appleVerifyClient.postForEntity(AUTH_TOKEN_URL, entity, AppleAccessTokenResponse.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private boolean verifyIdentityToken(String accessToken) throws ParseException {
        val signedJWT = SignedJWT.parse(accessToken);
        val payload = signedJWT.getJWTClaimsSet();

        val currentTime = new Date(System.currentTimeMillis());
        if (!currentTime.before(payload.getExpirationTime())) {
            return false;
        }

        if (!AUD.equals(payload.getIssuer()) || !SUB.equals(payload.getAudience().get(0))) {
            return false;
        }

        if (verifyPublicKey(signedJWT)) {
            return true;
        }
        return false;
    }

}
