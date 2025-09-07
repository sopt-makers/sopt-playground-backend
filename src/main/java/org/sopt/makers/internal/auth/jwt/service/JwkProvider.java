package org.sopt.makers.internal.auth.jwt.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.PublicKey;
import java.text.ParseException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.auth.external.auth.AuthClient;
import org.sopt.makers.internal.auth.external.exception.ClientException;
import org.sopt.makers.internal.auth.jwt.code.JwkFailure;
import org.sopt.makers.internal.auth.jwt.exception.JwkException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwkProvider {

    private final Cache<String, PublicKey> keyCache;
    private final AuthClient authClient;

    public JwkProvider(AuthClient authClient) {
        this.keyCache = Caffeine.newBuilder()
                .maximumSize(20)
                .expireAfterWrite(Duration.ofDays(1))
                .build();
        this.authClient = authClient;
    }

    public PublicKey getPublicKey(String kid) {
        if (kid == null || kid.isBlank()) {
            throw new JwkException(JwkFailure.JWK_KID_MISSING);
        }

        // 캐시에서 public key 조회, 없으면 resolvePublicKey를 통해 새로 조회
        return keyCache.get(kid, this::resolvePublicKey);
    }

    private PublicKey resolvePublicKey(String kid) {
        // 인증 서버에서 JWK 세트 받아와서 JWKSet 객체로 파싱
        try {
            String json = authClient.getJwk();
            JWKSet jwkSet = JWKSet.parse(json);

            // JWK 세트 순회하며 KID에 해당하는 JWK 찾기
            JWK matchedJwk = jwkSet.getKeys().stream()
                    .filter(key -> kid.equals(key.getKeyID()))
                    .findFirst()
                    .orElseThrow(() -> new JwkException(JwkFailure.JWK_KID_NOT_FOUND));

            // JWK -> PublicKey 변환 (로더는 절대 null 반환 금지)
            PublicKey pk = convertToPublicKey(matchedJwk);
            if (pk == null) {
                throw new JwkException(JwkFailure.JWK_INVALID_FORMAT);
            }
            return pk;
        }  catch (JwkException | ClientException e) {
            throw e;
        } catch (RuntimeException | ParseException e) {
            log.error(e.getMessage());
            throw new JwkException(JwkFailure.JWK_FETCH_FAILED);
        }
    }

    private PublicKey convertToPublicKey(JWK jwk) {
        // JWK가 RSA키인지 확인
        if (!(jwk instanceof RSAKey rsaKey)) {
            throw new JwkException(JwkFailure.JWK_INVALID_FORMAT);
        }

        try {
            // RSA JWK를 RSAPublicKey 객체로 변환
            return rsaKey.toRSAPublicKey();
        } catch (JOSEException e) {
            throw new JwkException(JwkFailure.JWK_INVALID_FORMAT);
        }
    }

    /**
     * 캐시 무효화 (예: 복호화 실패 시)
     */
    public void invalidateKey(String kid) {
        log.warn("Invalidating cached JWK for kid={}", kid);
        keyCache.invalidate(kid);
    }
}
